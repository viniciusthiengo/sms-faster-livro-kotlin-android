package thiengo.com.br.smsfaster

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), TextWatcher, EasyPermissions.PermissionCallbacks {

    /*
     * Para trabalharmos com constante em Kotlin. Ou é definida em alto nível,
     * fora de classe, ou em um object (como abaixo).
     * */
    companion object {
        const val SMS_AND_PHONE_STATE_REQUEST_CODE = 2256 // Inteiro aleatório
    }


    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )
        setContentView( R.layout.activity_main )

        et_message.addTextChangedListener( this )
    }


    /*
     * Para que seja possível contar os caracteres do campo de
     * mensagem e então atualizar o contador em tela.
     * */
    override fun onTextChanged( text: CharSequence, start: Int, before: Int, count: Int ) {
        tv_counter.text = String.format( "%d / %s", text.length, getString( R.string.max ) )
    }
    override fun afterTextChanged(p0: Editable?) {}
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


    // Para limpar o campo de mensagem.
    fun clearMessage( view: View ){
        et_message.text.clear()
    }


    /*
     * Listener de clique do botão enviar que na verdade iniciará
     * solicitando as permissões necessárias.
     * */
    fun sendSMS( view: View ){
        EasyPermissions
            .requestPermissions(
                this,
                getString( R.string.rationale_sms_phone_state_permissions ),
                SMS_AND_PHONE_STATE_REQUEST_CODE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE );
    }

    /*
     * Método de envio de SMS.
     * */
    private fun sendSMS(){
        try {
            val number = String.format("+%s%s%s", et_ddi.text, et_ddd.text, et_number.text)
            val message = et_message.text.toString()
            val smsManager = SmsManager.getDefault()

            smsManager.sendTextMessage(
                    number,
                null,
                    message,
                null,
                null )

            toast( R.string.sms_sent_successful )
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            toast( R.string.sms_error )
        }
    }


    /*
     * Método utilizado para encapsular todo o boilerplate code da
     * API Toast.
     * */
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

    /*
     * Método de permissão(ões) negada(s). Neste caso passa por todo um algoritmo
     * para saber qual mensagem apresentar e como apresenta-la de acordo
     * com a permissão que foi negada e se o box "Não perguntar novamente"
     * foi marcado.
     * */
    override fun onPermissionsDenied( requestCode: Int, perms: MutableList<String> ) {
        var title = getString( R.string.title_needed_permission )
        lateinit var rationale: String
        lateinit var toast: String
        val permissions = mutableListOf<String>()

        /*
         * Obtendo as mensagens de razão e toast, título e permissões
         * (negadas) corretas.
         * */
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
        else {
            rationale = getString(R.string.rationale_needed_phone_permission)
            toast = getString(R.string.toast_needed_phone_permission)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        /*
         * É necessário obter somente as permissões negadas em permissions,
         * pois caso contrário o box de AppSettingsDialog será apresentado
         * mesmo quando o usuário não tenha marcado a opção "Não perguntar
         * novamente".
         * */
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

    /*
     * Método (nativo) que contém o algoritmo responsável por apresentar uma
     * mensagem ao usuário caso ainda falte (ou não) alguma permissão a ser
     * concedida, isso depois da volta da área de configurações do aplicativo.
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE ){
            var toastContent = R.string.toast_perms_granted

            /*
             * Escolhendo a mensagem correta a ser apresentada na API Toast
             * caso alguma (ou ambas) permissão ainda não tenha sido fornecida.
             * */
            if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
                    && !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){
                toastContent = R.string.toast_perms_not_yet_granted
            }
            else if( !EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS) ){
                toastContent = R.string.toast_perm_sms_not_yet_granted
            }
            else if( !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){
                toastContent = R.string.toast_perm_phone_not_yet_granted
            }

            toast( toastContent )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if( EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
            && EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE) ){

            sendSMS()
        }
    }
}
