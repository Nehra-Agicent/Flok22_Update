package com.flok22.android.agicent.network

import com.flok22.android.agicent.model.disconnect.DisconnectModel
import com.flok22.android.agicent.model.LatLngModel
import com.flok22.android.agicent.model.NearByPlaceResponse
import com.flok22.android.agicent.model.acceptRequest.RequestModel
import com.flok22.android.agicent.model.acceptRequest.RequestResponse
import com.flok22.android.agicent.model.allChatMsg.AllChatMessageModel
import com.flok22.android.agicent.model.allChatMsg.AllChatMessageResponse
import com.flok22.android.agicent.model.aws.AwsCredentialResponse
import com.flok22.android.agicent.model.blockUser.BlockUserModel
import com.flok22.android.agicent.model.blockUser.BlockUserResponse
import com.flok22.android.agicent.model.blockedUser.BlockedUsers
import com.flok22.android.agicent.model.cancelRequest.CancelRequestModel
import com.flok22.android.agicent.model.cancelRequest.CancelRequestResponse
import com.flok22.android.agicent.model.chat.AllChatsModel
import com.flok22.android.agicent.model.chat.AllChatsResponse
import com.flok22.android.agicent.model.chatInfo.ChatInfoModel
import com.flok22.android.agicent.model.chatInfo.ChatInfoResponse
import com.flok22.android.agicent.model.checkIn.PlaceDetailResponse
import com.flok22.android.agicent.model.checkIn.PlaceIdModel
import com.flok22.android.agicent.model.checkOut.CheckOutModel
import com.flok22.android.agicent.model.checkOut.CheckOutResponse
import com.flok22.android.agicent.model.checkedIn.CheckedInModel
import com.flok22.android.agicent.model.checkedIn.CheckedInResponse
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailModel
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailResponse
import com.flok22.android.agicent.model.connectionRequest.ConnectionRequestModel
import com.flok22.android.agicent.model.connectionRequest.ConnectionRequestResponse
import com.flok22.android.agicent.model.disconnect.DisconnectResponse
import com.flok22.android.agicent.model.historicalData.HistoryResponse
import com.flok22.android.agicent.model.login.LogInModel
import com.flok22.android.agicent.model.login.OtpResponse
import com.flok22.android.agicent.model.otp.OtpModel
import com.flok22.android.agicent.model.otp.OtpResult
import com.flok22.android.agicent.model.pendingRequest.PendingRequestResponse
import com.flok22.android.agicent.model.permanentChat.PermanentChatModel
import com.flok22.android.agicent.model.permanentChat.PermanentChatResponse
import com.flok22.android.agicent.model.privateAccount.AccountPrivateModel
import com.flok22.android.agicent.model.privateAccount.AccountPrivateResponse
import com.flok22.android.agicent.model.rejectRequest.RejectModel
import com.flok22.android.agicent.model.rejectRequest.RejectResponse
import com.flok22.android.agicent.model.signOut.ClearTokenResponse
import com.flok22.android.agicent.model.signOut.TokenModel
import com.flok22.android.agicent.model.signUp.RegisteredResponse
import com.flok22.android.agicent.model.signUp.SignUpModel
import com.flok22.android.agicent.model.signUp.UpdateDeviceTokenResponse
import com.flok22.android.agicent.model.signUp.UpdateTokenModel
import com.flok22.android.agicent.model.unblock.UnBlockResponse
import com.flok22.android.agicent.model.unblock.UnBlockUserBody
import com.flok22.android.agicent.model.updateProfile.UpdateProfileModel
import com.flok22.android.agicent.model.updateProfile.UpdateResponse
import com.flok22.android.agicent.model.userCheckedInStatus.UserCheckedInResponse
import com.flok22.android.agicent.model.userInfo.UserInfoModel
import com.flok22.android.agicent.model.userInfo.UserInfoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("send-phone-verification-otp")
    fun sendOtp(@Body logInModel: LogInModel): Call<OtpResponse>?

    @POST("verify-phone-verification-otp")
    fun verifyOtp(@Body otpModel: OtpModel): Call<OtpResult>?

    @POST("register-user")
    fun registerUser(@Body signUpModel: SignUpModel): Call<RegisteredResponse>?

    @POST("update-device-token")
    fun updateDeviceToken(
        @Header("auth_key") auth_key: String,
        @Body updateTokenModel: UpdateTokenModel
    ): Call<UpdateDeviceTokenResponse>?

    @POST("get-nearby-places")
    fun getNearByPlaces(
        @Header("auth_key") auth_key: String,
        @Body latLngModel: LatLngModel
    ): Call<NearByPlaceResponse>?

    @POST("update-profile")
    fun updateProfile(
        @Header("auth_key") auth_key: String,
        @Body updateProfileModel: UpdateProfileModel
    ): Call<UpdateResponse>?

    @POST("clear-device-token")
    fun clearDeviceToken(
        @Header("auth_key") auth_key: String,
        @Body tokenModel: TokenModel
    ): Call<ClearTokenResponse>?

    @POST("get-place-detail-by-id")
    fun getPlaceDetail(
        @Header("auth_key") auth_key: String,
        @Body placeIdModel: PlaceIdModel
    ): Call<PlaceDetailResponse>?

    @POST("checked-in-to-the-place")
    fun checkedIntoPlace(
        @Header("auth_key") auth_key: String,
        @Body checkedInModel: CheckedInModel
    ): Call<CheckedInResponse>?

    @POST("send-connection-request")
    fun sendConnectionRequest(
        @Header("auth_key") auth_key: String,
        @Body connectionRequestModel: ConnectionRequestModel
    ): Call<ConnectionRequestResponse>?

    @POST("get-user-all-pending-request")
    fun getPendingRequest(@Header("auth_key") auth_key: String): Call<PendingRequestResponse>?

    @POST("accept-connection-request")
    fun acceptRequest(
        @Header("auth_key") auth_key: String,
        @Body requestModel: RequestModel
    ): Call<RequestResponse>?

    @POST("reject-connection-request")
    fun rejectRequest(
        @Header("auth_key") authKey: String,
        @Body rejectModel: RejectModel
    ): Call<RejectResponse>?

    @POST("checked-out-user")
    fun checkOutUser(
        @Header("auth_key") authKey: String,
        @Body checkOutModel: CheckOutModel
    ): Call<CheckOutResponse>?

    @POST("get-all-chats")
    fun getAllChats(
        @Header("auth_key") authKey: String,
        @Body allChatsModel: AllChatsModel
    ): Call<AllChatsResponse>?

    @POST("get-all-chat-messages")
    fun getAllChatMessages(
        @Header("auth_key") authKey: String,
        @Body allChatMessageModel: AllChatMessageModel
    ): Call<AllChatMessageResponse>?

    @POST("cancel-connection-request")
    fun cancelConnectionRequest(
        @Header("auth_key") authKey: String,
        @Body cancelRequestModel: CancelRequestModel
    ): Call<CancelRequestResponse>?

    @POST("get-user-current-checked-in-status")
    fun getUserCheckedInStatus(@Header("auth_key") authKey: String): Call<UserCheckedInResponse>?

    @POST("get-checked-in-user-info")
    fun getCheckedInUserInfo(
        @Header("auth_key") authKey: String,
        @Body userInfoModel: UserInfoModel
    ): Call<UserInfoResponse>?


    @POST("update-user-privacy")
    fun makeAccountPrivate(
        @Header("auth_key") authKey: String,
        @Body accountPrivateModel: AccountPrivateModel
    ): Call<AccountPrivateResponse>?

    @POST("block-user")
    fun blockUser(
        @Header("auth_key") authKey: String,
        @Body blockUserModel: BlockUserModel
    ): Call<BlockUserResponse>?

    @POST("reject-permanent-chat-request")
    fun rejectPermanentRequest(
        @Header("auth_key") authKey: String,
        @Body permanentChatModel: PermanentChatModel
    ): Call<PermanentChatResponse>?

    @POST("accept-permanent-chat-request")
    fun acceptPermanentRequest(
        @Header("auth_key") authKey: String,
        @Body permanentChatModel: PermanentChatModel
    ): Call<PermanentChatResponse>?

    @POST("get-checked-in-user-details-by-id")
    fun getCheckedInUserDetail(
        @Header("auth_key") authKey: String,
        @Body checkedInUserDetailModel: CheckedInUserDetailModel
    ): Call<CheckedInUserDetailResponse>?

    @POST("get-aws-credentials")
    fun getAwsCredentials(): Call<AwsCredentialResponse>?

    @POST("get-all-blocked-user")
    fun getBlockedUsers(@Header("auth_key") authKey: String): Call<BlockedUsers>?

    @POST("get-historical-data")
    fun getHistoricalData(@Header("auth_key") authKey: String): Call<HistoryResponse>?

    @POST("delete-account")
    fun deleteAccount(@Header("auth_key") authKey: String): Call<UpdateResponse>?

    @POST("un-block-user")
    fun unBlockUser(
        @Header("auth_key") authKey: String,
        @Body unBlockUserBody: UnBlockUserBody
    ): Call<UnBlockResponse>?

    @POST("disconnect-chat")
    fun disconnect(
        @Header("auth_key") authKey: String,
        @Body disconnectModel: DisconnectModel
    ): Call<DisconnectResponse>?

    @POST("get-chat-info-by-id")
    fun getChatInfo(
        @Header("auth_key") authKey: String,
        @Body chatInfoModel: ChatInfoModel
    ): Call<ChatInfoResponse>?
}