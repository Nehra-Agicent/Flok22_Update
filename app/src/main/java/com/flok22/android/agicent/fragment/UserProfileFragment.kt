package com.flok22.android.agicent.fragment

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.bottomsheet.UserSettingsBottomSheet
import com.flok22.android.agicent.databinding.FragmentUserProfileBinding
import com.flok22.android.agicent.model.updateProfile.UpdateProfileModel
import com.flok22.android.agicent.model.updateProfile.UpdateResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.aws.S3Utils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : Fragment() {

    private val className = UserProfileFragment::class.java.simpleName
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var name: String? = null
    private var dob: String? = null
    private var gender: String? = null
    private var bio: String? = null
    private var profilePic: String? = null
    private lateinit var datePicker: SingleDateAndTimePickerDialog.Builder
    private var date: String? = null
    private var isDateValid: Boolean? = null
    private var imageUrl = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefHelper = SharedPreferenceManager(requireContext())
        datePicker = SingleDateAndTimePickerDialog.Builder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get data to store
        name = prefHelper.name
        dob = prefHelper.dob
        gender = getGender(prefHelper.gender)
        bio = prefHelper.bio
        profilePic = prefHelper.profilePic

        setDateOfBirth()

        //disable clicks
        binding.dpContainer.isClickable = false
        binding.cameraContainer.isClickable = false
        binding.userName.isEnabled = false
        binding.dateOfBirth.isClickable = false
        binding.calenderButton.isClickable = false
        binding.genderSpinner.isEnabled = false
        binding.bioTextInput.isEnabled = false

        //show on ui
        binding.updateProfile.hide()
        binding.editProfile.show()
        binding.userName.setText(name)
        binding.dateOfBirth.text = dob
        binding.gender.setText(gender)
        binding.bioEt.setText(bio)

        if (!profilePic.isNullOrEmpty()) {
            Glide.with(binding.userProfileIcon.context)
                .load(S3Utils.generateS3ShareUrl(context, profilePic)).centerCrop()
                .into(binding.userProfileIcon)
        }

        binding.bioEt.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        /**
         * Show bottom sheet dialog for profile settings.
         */
        binding.profileSettings.setOnClickListener {
            UserSettingsBottomSheet.newInstance().show(childFragmentManager, "")
        }

        // Edit profile
        binding.editProfile.setOnClickListener {
            // show update button and hide edit button
            binding.updateProfile.show()
            binding.editProfile.hide()

            // make views clickable
            binding.dpContainer.isClickable = true
            binding.cameraContainer.isClickable = true
            binding.userName.isEnabled = true
            binding.dateOfBirth.isClickable = true
            binding.calenderButton.isClickable = true
            binding.genderSpinner.isEnabled = true
            binding.bioTextInput.isEnabled = true
            setUserProfileImage()
            setGenderSpinner()

        }

        binding.updateProfile.setOnClickListener {

            // get values
            val name = binding.userName.text
            val dob = binding.dateOfBirth.text
            val gender = binding.gender.text
            val bio = binding.bioEt.text
            if (imageUrl.isEmpty()) {
                imageUrl = prefHelper.profilePic
            }
            val url = imageUrl

            if (gender.toString().equals("select", true)) {
                requireContext().showToast("Please Select Male or Female")
                return@setOnClickListener
            }

            val updateProfileModel = UpdateProfileModel(
                bio = bio.toString(),
                dob = dob.toString(),
                full_name = name.toString(),
                gender = setGender(gender.toString()),
                profile_pic = url,
                phone_num = prefHelper.phoneNum
            )
            if (validateFields(requireContext(), name, dob, gender, bio, url))
                updateProfile(updateProfileModel)
        }

    }

    private fun setDateOfBirth() {
        binding.dateOfBirth.setOnClickListener {
            requireContext().hideKeyboard(it)
            showDateDialog()
        }
        binding.calenderButton.setOnClickListener {
            requireContext().hideKeyboard(it)
            showDateDialog()
        }
    }

    private fun showDateDialog() {
        datePicker.bottomSheet()
            .curved()
            .displayMinutes(false)
            .backgroundColor(Color.DKGRAY)
            .mainColor(resources.getColor(R.color.base_color))
            .titleTextColor(Color.WHITE)
            .displayHours(false)
            .displayDays(false)
            .displayMonth(true)
            .displayYears(true)
            .displayDaysOfMonth(true)
            .listener {
                val strCurrentDate = it.toString()
                changeDateFormat(strCurrentDate)
                if (isDateValid == true) {
                    binding.dateOfBirth.text = date
                } else {
                    requireContext().showToast("Please choose date before today.")
                }
            }
            .display()
    }

    private fun setGenderSpinner() {
        binding.gender.setOnClickListener {
            requireContext().hideKeyboard(it)
        }
        val gender = resources.getStringArray(R.array.gender)
        val adapter = ArrayAdapter(requireContext(), R.layout.gender_list_item, gender)
        binding.gender.setAdapter(adapter)
    }

    private fun updateProfile(updateProfileModel: UpdateProfileModel) {
        val call = RetrofitBuilder.apiService.updateProfile(prefHelper.authKey, updateProfileModel)
        call?.enqueue(object : Callback<UpdateResponse?> {
            override fun onResponse(
                call: Call<UpdateResponse?>,
                response: Response<UpdateResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        //change button visibility
                        binding.updateProfile.hide()
                        binding.editProfile.show()

                        // make views not clickable
                        binding.userName.isEnabled = false
                        binding.bioTextInput.isEnabled = false
                        binding.genderSpinner.isEnabled = false
                        binding.dateOfBirth.isClickable = false
                        binding.calenderButton.isClickable = false
                        binding.dpContainer.isClickable = false
                        binding.cameraContainer.isClickable = false

                        // update shared prefs
                        prefHelper.name = binding.userName.text.toString()
                        prefHelper.bio = binding.bioEt.text.toString()
                        prefHelper.gender = setGender(binding.gender.text.toString())
                        prefHelper.dob = binding.dateOfBirth.text.toString()
                        prefHelper.profilePic = imageUrl
                        requireContext().showToast("${response.body()?.message}")
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<UpdateResponse?>, t: Throwable) {
                requireContext().showToast("Failed to update profile at the moment")
                Log.d(TAG, "$className updateProfile() onFailure: $t")
            }
        })
    }

    private fun getGender(gender: String): String {
        return when (gender) {
            "M" -> {
                "Male"
            }
            "F" -> {
                "Female"
            }
            else -> {
                "Other"
            }
        }
    }

    private fun setGender(gender: String): String {
        return when (gender) {
            "Male" -> {
                "M"
            }
            "Female" -> {
                "F"
            }
            else -> {
                "O"
            }
        }
    }

    private fun changeDateFormat(strCurrentDate: String) {
        var format = SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy", Locale.US)
        val newDate = format.parse(strCurrentDate)
        isValidDate(newDate)
        format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        date = newDate?.let { it1 -> format.format(it1) }
    }

    private fun isValidDate(date: Date?) {
        isDateValid = date!!.time <= System.currentTimeMillis()
    }

    private fun setUserProfileImage() {
        binding.dpContainer.setOnClickListener {
            requireContext().hideKeyboard(it)
            ImagePicker.with(this).crop().compress(1024)
                .maxResultSize(650, 650)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding.cameraContainer.setOnClickListener {
            requireContext().hideKeyboard(it)
            ImagePicker.with(this).crop().compress(1024)
                .maxResultSize(650, 650)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    private var file: File? = null

    /*@Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                binding.userProfileIcon.apply {
                    setImageURI(uri)
                    setPadding(0)
                }
                uri.path.let {
                    file = File(it!!)
                }
                uploadFileToS3(file)
            }
            ImagePicker.RESULT_ERROR -> {
                requireContext().showToast(ImagePicker.getError(data))
            }
        }
    }*/

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val uri: Uri = data?.data!!
                    binding.userProfileIcon.apply {
                        setImageURI(uri)
                        setPadding(0)
                    }
                    uri.path.let {
                        file = File(it!!)
                    }
                    uploadFileToS3(file)
                }
                ImagePicker.RESULT_ERROR -> {
                    requireContext().showToast(ImagePicker.getError(data))
                }
                else -> {
                    requireContext().showToast("Task Cancelled")
                }
            }
        }

    private fun uploadFileToS3(file: File?) {
        val fileName =
            "profile_image/${getYearMonth()}/img_${System.currentTimeMillis()}.png"
        val transferUtility = S3Utils.getTransferUtility(requireContext())
        val observer: TransferObserver = transferUtility!!.upload(
            DeviceTokenPref(requireContext()).bucketName,
            fileName,
            file,
            CannedAccessControlList.Private
        )
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    imageUrl = fileName
                    Log.d(TAG, "$className uploadFileToS3() onStateChanged: $imageUrl")
                } else if (state == TransferState.FAILED) {
                    requireContext().showToast("Failed to upload,try later")
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                Log.d(
                    TAG, String.format(
                        "onProgressChanged: %d, total: %d, current: %d", id, bytesTotal,
                        bytesCurrent
                    )
                )
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.e(TAG, "$className Error during upload: $id", ex)
            }
        })
    }
}