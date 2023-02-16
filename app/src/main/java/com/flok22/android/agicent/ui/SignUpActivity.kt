package com.flok22.android.agicent.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.flok22.android.agicent.MainActivity
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.ActivitySignUpBinding
import com.flok22.android.agicent.model.signUp.*
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

class SignUpActivity : AppCompatActivity() {

    private val className = SignUpActivity::class.java.simpleName
    private lateinit var binding: ActivitySignUpBinding
    private var countryCode: String? = null
    private var phoneNumber: String? = null
    private var userData: Data? = null
    private var imageUrl = ""
    private lateinit var prefHelper: SharedPreferenceManager
    private lateinit var datePicker: SingleDateAndTimePickerDialog.Builder
    private var isDateValid: Boolean? = null
    private var date: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            countryCode = extras.getString("countryCode")
            phoneNumber = extras.getString("phoneNumber")
        }
        prefHelper = SharedPreferenceManager(this)
        datePicker = SingleDateAndTimePickerDialog.Builder(this)

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.genderTv.setOnClickListener {
            hideKeyboard(it)
        }

        setUserProfileImage()
//        initDatePicker()
        setDateOfBirth()
        setGender()

        binding.editBio.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        binding.signupBu.setOnClickListener {
            val name = binding.nameTv.text
            val dOB = binding.dobTv.text
            val gender = binding.genderTv.text
            val bio = binding.editBio.text
            if (validateFields(applicationContext, name, dOB, gender, bio, imageUrl)
            ) {
                if (isNetworkAvailable()) {
                    val signUpModel = SignUpModel(
                        fullName = name.toString(),
                        phoneNum = phoneNumber!!,
                        countryCode = countryCode!!,
                        email = "abd_123@gmail.com",
                        dateOfBirth = dOB.toString(),
                        profilePic = imageUrl,
                        gender = getGender(gender),
                        bio = bio.toString(),
                        deviceType = "A",
                        deviceToken = DeviceTokenPref(this).deviceToken
                    )
                    Log.i(TAG, "$className onCreate: $signUpModel")
                    registerUser(signUpModel)
                } else showNetworkToast()
            }
        }
    }

    private fun getGender(text: Editable?): String {
        return if (text?.toString() == "Male") {
            "M"
        } else if (text?.toString() == "Female") {
            "F"
        } else "O"
    }

    private fun uploadFileToS3(file: File?) {
        val fileName =
            "profile_image/${getYearMonth()}/img_${System.currentTimeMillis()}.png"
        val transferUtility = S3Utils.getTransferUtility(this)
        val observer: TransferObserver = transferUtility!!.upload(
            DeviceTokenPref(this).bucketName,
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
                    showToast("Failed to upload,try later")
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

    private var datePickerDialog: DatePickerDialog? = null

    private fun initDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val date = makeDateString(day, month + 1, year)
            binding.dobTv.text = date
        }
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val style = AlertDialog.THEME_HOLO_LIGHT
        datePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)
        datePickerDialog!!.datePicker.maxDate = System.currentTimeMillis()
    }

    private fun setUserProfileImage() {
        binding.profileImage.setOnClickListener {
            hideKeyboard(it)
            ImagePicker.with(this).crop().compress(1024)
                .maxResultSize(650, 650)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
        binding.cameraView.setOnClickListener {
            hideKeyboard(it)
            ImagePicker.with(this).crop().compress(1024)
                .maxResultSize(650, 650)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    private var file: File? = null
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                binding.userImage.apply {
                    setImageURI(uri)
                    setPadding(0)
                }
                uri.path.let {
                    file = File(it!!)
                }
                uploadFileToS3(file)
            }
            ImagePicker.RESULT_ERROR -> {
                showToast(ImagePicker.getError(data))
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
                    binding.userImage.apply {
                        setImageURI(uri)
                        setPadding(0)
                    }
                    uri.path.let {
                        file = File(it!!)
                    }
                    uploadFileToS3(file)
                }
                ImagePicker.RESULT_ERROR -> {
                    showToast(ImagePicker.getError(data))
                }
                else -> {
                    showToast("Task Cancelled")
                }
            }
        }

    private fun setDateOfBirth() {
        binding.dobTv.setOnClickListener {
            hideKeyboard(it)
            showDateDialog()
        }
        binding.dobTextInput.setOnClickListener {
            hideKeyboard(it)
            showDateDialog()
        }
        binding.calender.setOnClickListener {
            hideKeyboard(it)
            showDateDialog()
        }
    }

    /*private fun showDateDialog() {
        datePickerDialog?.show()
    }*/

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
                    binding.dobTv.text = date
                } else {
                    showToast("Please choose date before today.")
                }
            }
            .display()
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

    private fun setGender() {
        val gender = resources.getStringArray(R.array.gender)
        val adapter = ArrayAdapter(this, R.layout.gender_list_item, gender)
        binding.genderTv.setAdapter(adapter)
    }

    private fun registerUser(signUpModel: SignUpModel) {
        val call = RetrofitBuilder.apiService.registerUser(signUpModel)
        call?.enqueue(object : Callback<RegisteredResponse?> {
            var registerUser: RegisteredResponse? = null
            override fun onResponse(
                call: Call<RegisteredResponse?>,
                response: Response<RegisteredResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        registerUser = response.body()
                        userData = registerUser?.data
                        Log.d(
                            TAG,
                            "$className registerUser() onResponse auth: ${userData?.auth_Key}"
                        )
                        val updateTokenModel =
                            UpdateTokenModel(signUpModel.deviceToken, signUpModel.deviceType)
                        val authKey = userData!!.auth_Key
                        prefHelper.authKey = authKey
                        prefHelper.deviceType = "A"
                        //save user data
                        prefHelper.profilePic = userData!!.profile_pic
                        prefHelper.userId = userData!!.user_id
                        prefHelper.name = userData!!.full_name
                        prefHelper.dob = userData!!.dob
                        prefHelper.gender = userData!!.gender
                        prefHelper.bio = userData!!.bio
                        updateDeviceToken(authKey, updateTokenModel)
                    }
                    500 -> {
                        showToast("Database Error")
                    }
                }
            }

            override fun onFailure(call: Call<RegisteredResponse?>, t: Throwable) {
                Log.d(TAG, "$className registerUser() onFailure: $t")
            }
        })
    }

    private fun updateDeviceToken(authKey: String, updateTokenModel: UpdateTokenModel) {
        val call = RetrofitBuilder.apiService.updateDeviceToken(authKey, updateTokenModel)
        call?.enqueue(object : Callback<UpdateDeviceTokenResponse?> {
            override fun onResponse(
                call: Call<UpdateDeviceTokenResponse?>,
                response: Response<UpdateDeviceTokenResponse?>
            ) {
                if (response.isSuccessful) {
                    Log.i(
                        TAG,
                        "$className updateDeviceToken Response.isSuccessful: ${response.isSuccessful}"
                    )
                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }

            override fun onFailure(call: Call<UpdateDeviceTokenResponse?>, t: Throwable) {
                Log.d(TAG, "$className updateDeviceToken onFailure: $t")
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}