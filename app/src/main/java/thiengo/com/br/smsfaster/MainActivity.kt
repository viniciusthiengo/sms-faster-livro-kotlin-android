package thiengo.com.br.smsfaster

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), TextWatcher, EasyPermissions.PermissionCallbacks {

    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )
        setContentView( R.layout.activity_main )

        et_message.addTextChangedListener( this )
    }


    // Para contador de caracteres no campo de mensagem.
    override fun onTextChanged( text: CharSequence?, start: Int, before: Int, count: Int ) {
        tv_counter.text = String.format( "%d / %s", count, getString( R.string.max ) )
    }
    override fun afterTextChanged(p0: Editable?) {}
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


    // Para limpar o campo de mensagem.
    fun clearMessage( view: View ){
        et_message.text.clear()
    }


    // Para envio de SMS.
    fun sendSMS( view: View ){
        EasyPermissions
            .requestPermissions(
                this,
                getString( R.string.rationale_sms_phone_state_permissions ),
                SMS_AND_PHONE_STATE_REQUEST_CODE,
                Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE );
    }

    private fun sendSMS(){
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                    "+5527997867327",
                    null,
                    "Teste app.",
                    null,
                    null )

            toast( R.string.sms_sent_successful )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            toast( R.string.sms_error )
        }
    }

    private fun toast(messageId: Int){
        Toast
            .makeText(
                this,
                getString( messageId ),
                Toast.LENGTH_LONG )
            .show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied( requestCode: Int, perms: MutableList<String> ) {
        Log.i("LOG", "PERMS: $perms")

        var title = getString( R.string.title_needed_permission )
        lateinit var rationale: String
        lateinit var toast: String
        val permissions = mutableListOf<String>()

        if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
                && !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){

            title = getString( R.string.title_needed_permissions )
            rationale = getString(R.string.rationale_needed_permissions)
            toast = getString(R.string.toast_needed_permissions)
            permissions.add(Manifest.permission.SEND_SMS)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        else if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS) ){
            rationale = getString(R.string.rationale_needed_sms_permission)
            toast = getString(R.string.toast_needed_sms_permission)
            permissions.add(Manifest.permission.SEND_SMS)
        }
        else if( !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){
            rationale = getString(R.string.rationale_needed_phone_permission)
            toast = getString(R.string.toast_needed_phone_permission)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if( EasyPermissions.somePermissionPermanentlyDenied(this, permissions) ){
            // Caso o "Não perguntar novamente" tenha sido marcado.

            AppSettingsDialog
                .Builder(this)
                    .setTitle( title )
                    .setRationale( rationale )
                    .build()
                    .show()
        }
        else{
            Toast
                .makeText(
                    applicationContext,
                    toast,
                    Toast.LENGTH_LONG )
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("LOG", "onActivityResult($requestCode)")

        if( requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE ){
            var toastId = 0

            if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
                    && !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){

                toastId = R.string.toast_perms_not_yet_granted
            }
            else if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS) ){
                toastId = R.string.toast_perm_sms_not_yet_granted
            }
            else if( !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){
                toastId = R.string.toast_perm_phone_not_yet_granted
            }

            Log.i("LOG", "Toast: $toastId")

            if( toastId != 0 ){
                toast( toastId )
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if( EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
            && EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){

            sendSMS()
        }
    }


    // Necessário para trabalharmos com constante em Kotlin. Ou é definida em alto nível, fora de classe, ou em um object.
    companion object {
        const val SMS_AND_PHONE_STATE_REQUEST_CODE = 2256 // Inteniro aleatório
    }
}
