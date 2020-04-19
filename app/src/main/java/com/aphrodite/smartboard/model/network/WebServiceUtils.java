package com.aphrodite.smartboard.model.network;

import android.content.Context;

import com.aphrodite.framework.utils.NetworkUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

public class WebServiceUtils {

    public static String user = "";
    public static String pass = "";
    public static final String REQ_STATUS_TRUE = "true";
    public static final String REQ_STATUS_FALSE = "false";
    public static final String REQ_STATUS_KEY = "status";
    public static final String REQ_RET_KEY = "ret";
    public static final String REQ_RET_VALUES = "";

    public static String WebServiceURL = "http://120.55.71.27/IntGlass/service1.asmx";
    public static final String WebServiceNameSpace = "http://IntGlass.com/";

    //***********************intelglass***********************
    public static final String NET_URL_REGUSER = "RegUser";
    public static final String[] NET_URL_REGUSER_KEY = {"PhoneNo", "ret"};

    public static final String NET_URL_LOGIN = "Login";
    public static final String[] NET_URL_LOGIN_KEY = {"PhoneNo", "sms", "ret"};

    public static final String NET_URL_REPORERR = "ReportErr";
    public static final String[] NET_URL_REPORERR_KEY = {"PhoneNo", "sms", "msg", "ret"};

    public static final String NET_URL_SAVE_USER_DATA = "SaveUserData";
    public static final String[] NET_URL_SAVE_USER_DATA_KEY = {"PhoneNo", "sms", "BabyName", "ClassName", "Guardian", "Address", "BabySex", "BabyBirthDay", "Nation", "Org", "ret"};

    public static final String NET_URL_CHECK_VERSION = "GetVersion";
    public static final String[] NET_URL_CHECK_VERSION_KEY = {"PhoneNo", "ret"};

    public static final String NET_URL_SAVE_DATA = "SaveData";
    public static final String[] NET_URL_SAVE_DATA_KEY = {"PhoneNo", "sms", "data", "DesType", "ret"};

    public static final String NET_URL_SAVE_USER_SET = "SaveUserSet";
    public static final String[] NET_URL_SAVE_USER_SET_KEY = {"PhoneNo", "sms", "DesType", "Des", "LR", "FB", "Time1", "ret"};

    public static final String NET_URL_GET_USER_SET = "GetUserSet";
    public static final String[] NET_URL_GET_USER_SET_KEY = {"PhoneNo", "sms", "DesType", "Des", "LR", "FB", "Time1", "ret"};

    public static final String NET_URL_GET_DAY_REPORT_DATA = "GetDayReportData";
    public static final String[] NET_URL_GET_DAY_REPORT_DATA_KEY = {"PhoneNo", "sms", "RecTime", "destype", "Rep", "ret"};

    //***********************intelglass***********************

    public WebServiceUtils() {
        super();
    }

    public static boolean getWebServiceResult(String method, String[] key, List<String> values, List<String> list, Context context) {
        try {
            if (NetworkUtils.isNetworkAvailable(context)) {
                String webserviceURL = WebServiceURL;
                String webserviceNameSpace = WebServiceNameSpace;
                String soapAction = webserviceNameSpace + method;

                SoapObject request = new SoapObject(webserviceNameSpace, method);
                for (int i = 0; i < key.length; i++) {
                    request.addProperty(key[i], values.get(i));
                }

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                try {
                    HttpTransportSE ht = new HttpTransportSE(webserviceURL, 20000);
                    ht.call(soapAction, envelope);

                    SoapObject sb = (SoapObject) envelope.bodyIn;
                    for (int i = 0; i < sb.getPropertyCount(); i++) {
                        list.add(sb.getProperty(i).toString());
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    list.add(WebServiceUtils.REQ_STATUS_FALSE);
                    list.add(WebServiceUtils.REQ_RET_VALUES);
                    return false;
                }
            } else {
                ToastUtils.showMessage(context.getResources().getString(R.string.prompt_network_offline));
                list.add(WebServiceUtils.REQ_STATUS_FALSE);
                list.add(WebServiceUtils.REQ_RET_VALUES);
                return false;
            }
        } catch (Exception e) {
            list.add(WebServiceUtils.REQ_STATUS_FALSE);
            list.add(WebServiceUtils.REQ_RET_VALUES);
            return false;
        }
    }

//    public boolean getServiceGetNewID(long time) {
//        boolean flg = false;
//        //String ID;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        //MthGetNewID
//        values.add("6");
//        values.add("6");
//        values.add("6");
//        values.add("2014-8-5 12:30");//values.add(TimeUtil.getStringDate().toString());
//        values.add("" + time);
//        values.add("11");
//        values.add("ret");
//        if (true == getWebServiceResult(MthGetNewID, MthGetNewIDKey, values, list)) {
//            flg = true;
//            //ID=list.get(1);
//        }
//        //Debug.d(TAG,"MthGetNewID="+list.toString());
//        //SaveServiceStartTime.add(time);
//        list = null;
//        values = null;
//        return flg;
//    }
//
//    public static boolean helloWorld() {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//
//        if (true == getWebServiceResult(MthHelloWorld, MthHelloWorldKey, values, list)) {
//            flg = true;
//            //ID=list.get(1);
//        }
//        //Debug.d(TAG,"MthGetNewID="+list.toString());
//        //SaveServiceStartTime.add(time);
//        list = null;
//        values = null;
//        return flg;
//    }
//
//    public static boolean GetNewID(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.name);
//        values.add(wsa.beginTime);
//        values.add(wsa.beginTimeStr);
//        values.add(wsa.ID);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthGetNewID, MthGetNewIDKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                wsa.ID = list.get(1);
//            } else {
//                wsa.ret = list.get(2);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean ChangeSoundNameByID(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.name);
//        values.add(wsa.ID);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthChangeSoundNameByID, MthChangeSoundNameByIDKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean GetDataList(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.begin);
//        values.add(wsa.XML);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthGetDataList, MthGetDataListKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                wsa.XML = list.get(1);
//            } else {
//                wsa.ret = list.get(2);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean GetDataCount(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.count);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthGetDataCount, MthGetDataCountKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                wsa.count = list.get(1);
//            } else {
//                wsa.ret = list.get(2);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean Login(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        //values.add(wsa.count);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthLogin, MthLoginKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                wsa.ret = list.get(1);
//                //wsa.count=list.get(1);
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean RepErr(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        //values.add(wsa.count);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthRepErr, MthRepErrKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                //wsa.count=list.get(1);
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean ChangePass(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.newPass);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthChangePass, MthChangePassKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                //wsa.count=list.get(1);
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean NewUser(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.userName);
//        values.add(wsa.sex);
//        values.add(wsa.babyName);
//        values.add(wsa.babySex);
//        values.add(wsa.regTime);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthNewUser, MthNewUserKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                //wsa.count=list.get(1);
//
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean ChangeUserSet(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.userName);
//        values.add(wsa.sex);
//        values.add(wsa.babyName);
//        values.add(wsa.babySex);
//        values.add(wsa.regTime);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthChangeUserSet, MthChangeUserSetKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                //wsa.count=list.get(1);
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean GetUserSet(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        values.add(wsa.userName);
//        values.add(wsa.sex);
//        values.add(wsa.babyName);
//        values.add(wsa.babySex);
//        values.add(wsa.regTime);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthGetUserSet, MthGetUserSetKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                wsa.userName = list.get(1);
//                wsa.sex = list.get(2);
//                wsa.babyName = list.get(3);
//                wsa.babySex = list.get(4);
//                wsa.regTime = list.get(5);
//            } else {
//                wsa.ret = list.get(6);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean ReportLostPass(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthReportPass, MthReportPassKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean ConfirmSMS(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(wsa.SMSCode);
//        values.add(wsa.newPass);
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthConfirmSMS, MthConfirmSMSKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//            } else {
//                wsa.ret = list.get(1);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }
//
//    public static boolean GetMessage(WebServiceArgu wsa) {
//        boolean flg = false;
//        List<String> values = new ArrayList<String>();
//        List<String> list = new ArrayList<String>();
//        values.add(user);
//        values.add(pass);
//        //title
//        values.add("");
//        //cotext
//        values.add("");
//        //URL
//        values.add("");
//        //count
//        values.add("");
//        values.add(wsa.ret);
//        if (true == getWebServiceResult(MthGetMessage, MthGetMessageKey, values, list)) {
//
//            if (list.get(0).equals("true")) {
//                flg = true;
//                //wsa.count=list.get(1);
//                wsa.babyName = list.get(1);
//                wsa.beginTime = list.get(2);
//                wsa.beginTimeStr = list.get(3);
//                wsa.count = list.get(4);
//
//            } else {
//                wsa.ret = list.get(5);
//            }
//        } else {
//            wsa.ret = "WebService ���ó���";
//        }
//        list = null;
//        values = null;
//        wsa.retValue = flg;
//        return flg;
//    }

}
