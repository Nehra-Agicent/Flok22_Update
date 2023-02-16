package com.flok22.android.agicent.utils.aws

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.amazonaws.AmazonServiceException
import com.amazonaws.ClientConfiguration
import com.amazonaws.HttpMethod
import com.amazonaws.Protocol
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ResponseHeaderOverrides
import com.flok22.android.agicent.utils.Constants
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.DeviceTokenPref
import java.io.File
import java.util.*

object S3Utils {
    private var amzonS3Client: AmazonS3Client? = null
    private var amzonS3: AmazonS3? = null
    private var credProvider: CognitoCachingCredentialsProvider? = null
    private var transferUtility: TransferUtility? = null
    private var deviceTokenPref: DeviceTokenPref? = null

    private fun getS3Client(context: Context?): AmazonS3? {
        if (amzonS3 == null) {
            deviceTokenPref = DeviceTokenPref(context!!)
            val credentials: AWSCredentials =
                BasicAWSCredentials(deviceTokenPref!!.accessKey, deviceTokenPref!!.secretKey)
            val clientConfig = ClientConfiguration()
            clientConfig.protocol = Protocol.HTTP
            amzonS3 = AmazonS3Client(credentials, Region.getRegion(Constants.MY_REGION))
        }
        return amzonS3
    }

    fun getTransferUtility(context: Context): TransferUtility? {
        if (transferUtility == null) {
            transferUtility = TransferUtility.builder()
                .s3Client(getS3Client(context))
                .context(context)
                .build()
        }
        return transferUtility
    }

    fun generateS3ShareUrl(applicationContext: Context?, filename: String?): String {
        val s3client: AmazonS3 = getS3Client(applicationContext)!!
        deviceTokenPref = DeviceTokenPref(applicationContext!!)
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60.toLong() // 1 hour.
        expiration.time = msec

        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(filename)

        val generatePresignedUrlRequest =
            GeneratePresignedUrlRequest(deviceTokenPref!!.bucketName, filename)
        generatePresignedUrlRequest.method = HttpMethod.GET
        generatePresignedUrlRequest.expiration = expiration
        generatePresignedUrlRequest.responseHeaders = overrideHeader
        val url = s3client.generatePresignedUrl(generatePresignedUrlRequest)
        Log.e(TAG, "S3Utils generateS3ShareUrl() Generated Url -  $url")
        return url.toString()
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}