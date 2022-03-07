package com.ados.myfanclub

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val TAG = "FirebaseService"
    private val fcmGroup = "fcmGroup"
    private val summaryID = 1234

    // 메세지가 수신되면 호출
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.notification != null){  // 서버에서 직접 보냈을 때
            sendNotification(remoteMessage.notification?.title,
                remoteMessage.notification?.body!!)
        } else if(remoteMessage.data.isNotEmpty()){ // 다른 기기에서 서버로 보냈을 때
            val title = remoteMessage.data["title"]!!
            val userId = remoteMessage.data["userId"]!!
            val message = remoteMessage.data["message"]!!

            println("메시지: $title, $userId, $message")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                sendMessageNotification(title, userId, message)
            }
            else{
                sendNotification(title, message)
            }
        }
    }

    // Firebase Cloud Messaging Server 가 대기중인 메세지를 삭제 시 호출
    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    // 메세지가 서버로 전송 성공 했을때 호출
    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    // 메세지가 서버로 전송 실패 했을때 호출
    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }

    // 새로운 토큰이 생성 될 때 호출
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        //필요하면 이 토큰을 앱서버에 저장하는 과정을 거치면 된다.
        //sendToken(token)
        sendRegistrationToServer(token)
    }

    // 서버에서 직접 보냈을 때
    private fun sendNotification(title: String?, body: String){
        println("서버에서 보낸 메시지 $title, $body")
        val intent : Intent
        if (IsRunApp.isForeground) { // 앱이 실행중일 때는 팬클럽 화면 호출
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } else { // 앱 처음부터 실행
            intent = Intent(this, SplashActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지

        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT) // 일회성

        val channelId = getString(R.string.default_notification_channel_id) // 채널 아이디
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.star_big_off)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setGroup(fcmGroup)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify((System.currentTimeMillis()/100).toInt(), notificationBuilder.build()) // 알림 생성
        notificationManager.notify(summaryID, getSummaryNotification(this, fcmGroup).build()) // 알림 생성
    }

    // 다른 기기에서 서버로 보냈을 때
    @RequiresApi(Build.VERSION_CODES.P)
    private fun sendMessageNotification(title: String, userId: String, body: String){
        val intent : Intent
        if (IsRunApp.isForeground) { // 앱이 실행중일 때는 팬클럽 화면 호출
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } else { // 앱 처음부터 실행
            intent = Intent(this, SplashActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // 액티비티 중복 생성 방지

        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //intent.putExtra("key", value);
        //val pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_MUTABLE OR PendingIntent.FLAG_UPDATE_CURRENT)

        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT) // 일회성

        // messageStyle 로
        val user: Person = Person.Builder()
            .setName("[팬클럽공지] $title")
            .setIcon(IconCompat.createWithResource(this,R.drawable.fan_club))
            .build()

        val message = NotificationCompat.MessagingStyle.Message(
            "${userId}\n${body}",
            System.currentTimeMillis(),
            user
        )
        val messageStyle = NotificationCompat.MessagingStyle(user)
            .addMessage(message)

        val channelId = getString(R.string.default_notification_channel_id) // 채널 아이디
        //val channelId = "channel_id_${System.currentTimeMillis()}" // 채널 아이디
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 소리
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // 제목
            .setContentText(body) // 내용
            .setStyle(messageStyle)
            .setSmallIcon(android.R.drawable.star_big_off)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setGroup(fcmGroup)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 예외처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "알림 메세지",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        //notificationManager.notify(0 , notificationBuilder.build()) // 알림 생성
        notificationManager.notify((System.currentTimeMillis()/100).toInt(), notificationBuilder.build()) // 알림 생성
        notificationManager.notify(summaryID, getSummaryNotification(this, fcmGroup).build()) // 알림 생성
        //notificationManager.notify((System.currentTimeMillis()/100).toInt(), notificationBuilder.build()) // 알림 생성


        //NotificationManagerCompat.from(this).notify(System.currentTimeMillis()/100).toInt(), notificationBuilder.build())
        //NotificationManagerCompat.from(this).notify(summaryNoti, getSummaryNoti(this).build())
    }

    private fun getSummaryNotification(context: Context, group: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setGroup(group)
            .setGroupSummary(true)
    }

    // 받은 토큰을 서버로 전송
    private fun sendRegistrationToServer(token: String){

    }
}