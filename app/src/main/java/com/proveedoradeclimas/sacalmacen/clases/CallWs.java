package com.proveedoradeclimas.sacalmacen.clases;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by Administrador on 14/09/2016.
 */
public class CallWs {

    private String SOAP_ACTION = "";
    private String NAMESPACE = "";
    private String METHOD_NAME = "";
    private String URL = "";
    private SoapObject request;
    private SoapSerializationEnvelope envelope;
    private String resultado;
    private String Error;

    public CallWs(String Url, String Espacio, String Metodo, String accion, boolean isHeader) {
        URL = Url;
        NAMESPACE = Espacio;
        SOAP_ACTION = accion;
        METHOD_NAME = Metodo;

        request = new SoapObject(NAMESPACE, METHOD_NAME);
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        // Declare the version of the SOAP request
        envelope.setOutputSoapObject(request);
        envelope.dotNet = true;
        if(isHeader) {
            envelope.headerOut = new Element[1];
        }
    }

    public boolean llamarWs() {

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, 300000);
        String result = null;

        try {
            // this is the actual part that will call the webservice
            androidHttpTransport.call(SOAP_ACTION, envelope);
        }
        catch (HttpResponseException httpEx) {
            httpEx.printStackTrace();
            Log.i("TAG", "9HttpTransportException");
            Log.i("TAG", (httpEx.getMessage() == null) ? "No message provided" : httpEx.getMessage());
            Error = (httpEx.getMessage() == null) ? "9HttpTransportException" : "9" + httpEx.getMessage();
            return false;
        }
        catch (SocketException s) {
            s.printStackTrace();
            Log.i("TAG", "9SocketException");
            Log.i("TAG", (s.getMessage() == null) ? "No message provided" : s.getMessage());
            Error = (s.getMessage() == null) ? "9SocketException" : "9" + s.getMessage();
            return false;
        }
        catch (SocketTimeoutException stEx) {
            stEx.printStackTrace();
            Log.i("TAG", "9SocketTimeoutException");
            Log.i("TAG", (stEx.getMessage() == null) ? "No message provided" : stEx.getMessage());
            Error = (stEx.getMessage() == null) ? "9SocketTimeoutException" : "9" + stEx.getMessage();
            return false;
        }
        catch (IOException ioEx) {
            ioEx.printStackTrace();
            Log.i("TAG", "9IOException");
            Log.i("TAG", (ioEx.getMessage() == null) ? "No message provided" : ioEx.getMessage());
            Error = (ioEx.getMessage() == null) ? "9IOException" : "9" + ioEx.getMessage();
            return false;
        }
        catch (XmlPullParserException xmlEx) {
            xmlEx.printStackTrace();
            Log.i("TAG", "9XmlPullParserException");
            Log.i("TAG", (xmlEx.getMessage() == null) ? "No message provided" : xmlEx.getMessage());
            Error = (xmlEx.getMessage() == null) ? "9XmlPullParserException" : "9" + xmlEx.getMessage();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "9Exception");
            Log.i("TAG", (e.getMessage() == null) ? "No message provided" : e.getMessage());
            Error = (e.getMessage() == null) ? "9Exception" : "9" + e.getMessage();
            return false;
        }

        try {
            // Get the SoapResult from the envelope body.
            result = envelope.getResponse().toString();
        }
        catch (SoapFault e) {
            e.printStackTrace();
            Error = "9" + e.getMessage();
            Log.i("TAG", "9SoapFaultException");
            Log.i("TAG", e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            Error = "9" + e.getMessage();
            Log.i("TAG", "9Exception");
            Log.i("TAG", e.getMessage());
            return false;
        }

        if (result != null) {
            resultado = result;
            return true;
        }
        else {
            Error = "9Sin respuesta";
        }

        return false;
    }

    public void agregarParametro(String Nombre, String Valor) {
        request.addProperty(Nombre, Valor);
    }

    public void agregarHeader(String PalabraSecreta, String PW) {
        Element h = new Element().createElement(NAMESPACE,
                "AuthenticationHeader");
        Element username = new Element().createElement(NAMESPACE, "SecretWord");
        username.addChild(Node.TEXT, PalabraSecreta);
        h.addChild(Node.ELEMENT, username);
        Element pass = new Element().createElement(NAMESPACE, "PW");
        pass.addChild(Node.TEXT, PW);
        h.addChild(Node.ELEMENT, pass);

        envelope.headerOut[0] = h;
    }

    public String obtenResultado() {
        return resultado;
    }

    public String obtenError() {
        return Error;
    }
}
