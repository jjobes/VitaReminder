package com.vitareminder.reminders;

import java.util.HashMap;
import java.util.Map;

import com.voxeo.tropo.Tropo;


/**
 * An automated voice message that is sent using the Tropo API.
 */
public class VoiceMessage
{
    private String phoneNumber;
    private String message;


    /**
     * The sole constructor.
     *
     * @param phoneNumber  the phone number to send the automated voice message to,
     *                     in the following format: "+15554443333"
     * @param message  the message content
     */
    public VoiceMessage(String phoneNumber, String message)
    {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }


    /**
     * Sends the automated voice message using the Tropo API.
     * The {@code phoneNumber} and {@code message} fields of this object are passed
     * in to the {@code params} map.
     * <p>
     * Tropo sessions use token authentication, so we must provide our application's
     * unique token when launching the session.
     * <p>
     * The {@code params} map, along with our application's token are sent to the Tropo
     * server with the call to {@code tropo.launchSession(token, params)}.  The parameters
     * are unpacked and injected into a small script located on the server at /www/SendVoice.js
     * <p>
     * The script is simply:
     * <p>
     * call(phoneNumber);
     * <br>
     * say(msg);
     */
    public void send()
    {
        new Thread(new Runnable() {

            @Override
            public void run()
            {
                String token = System.getenv("TROPO_TOKEN_VOICE_MESSAGE");

                Tropo tropo = new Tropo();

                Map<String, String> params = new HashMap<String, String>();

                params.put("phoneNumber", phoneNumber);
                params.put("msg", message);

                tropo.launchSession(token, params);
            }
        }).start();
    }

}  // end class VoiceMessage
