package dev.tinelix.jabwave.telegram.api;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Locale;

import androidx.annotation.Nullable;
import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.JabwaveApp;

public class TDLibClient implements Client.ResultHandler, Client.ExceptionHandler {

   private final Client client;
   private TdApi.TdlibParameters params;

   public TDLibClient(Context app_ctx) {
      this.params = new TdApi.TdlibParameters();
      params.applicationVersion = BuildConfig.VERSION_NAME;
      params.deviceModel = Build.MODEL;
      params.systemVersion = Build.VERSION.RELEASE;
      try {
         params.apiId = Integer.parseInt(BuildConfig.APP_TOKEN.split("\\.")[0]);
         params.apiHash = BuildConfig.APP_TOKEN.split("\\.")[1];
         params.databaseDirectory = app_ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         params.filesDirectory = app_ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         params.systemLanguageCode = Locale.getDefault().toString();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      this.client = Client.create(this, null,this);
      send(new TdApi.SetTdlibParameters(params), null);
      send(new TdApi.CheckDatabaseEncryptionKey(), null);
   }

   public TDLibClient(TdApi.TdlibParameters params) {
      this.params = params;
      this.client = Client.create(this, null,this);
      send(new TdApi.SetTdlibParameters(params), null);
   }

   @Override
   public void onResult(TdApi.Object object) {
      Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
              String.format("[RESULT] Class %s", object.getClass().getSimpleName())
      );
   }

   @Override
   public void onException(Throwable e) {
      Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
              String.format("[ERROR] %s: %s", e.getClass().getSimpleName(), e.getMessage())
      );
   }

   public void send(TdApi.Function function, ApiHandler handler) {
      if(handler != null) {
         client.send(
                 function, handler::onSuccess,
                 handler::onFail
         );
      } else {
         client.send(function, null);
      }
   }

   public void destroy() {
      client.close();
   }

   public interface ApiHandler {
      void onSuccess(TdApi.Object object);
      void onFail(Throwable throwable);
   }

   public static class Error extends java.lang.Error {
      public static final String INVALID_TDLIB_PARAMETERS = "invalid_tdlib_parameters";
      public static final String REQUIRED_AUTH_CODE = "required_auth_code";
      public static final String REQUIRED_PHONE_NUMBER = "required_phone_number";
      public static final String REQUIRED_CLOUD_PASSWORD = "required_cloud_password";
      public static final String REQUIRED_PREMIUM_SUBSCRIPTION = "required_premium_subscription";
      private final String tag;
      private final String message;

      public Error(String tag, String message) {
         this.tag = tag;
         this.message = message;
      }

      public String getTag() {
         return tag;
      }

      @Nullable
      @Override
      public String getMessage() {
         return message;
      }
   }
}
