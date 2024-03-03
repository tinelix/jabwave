package dev.tinelix.jabwave.telegram.api;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.Nullable;
import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.APISecureStorage;

public class TDLibClient implements Client.ResultHandler, Client.ExceptionHandler {

   private final Client client;
   public ApiHandler apiHandler;
   private ClientHandler handler;
   private TdApi.TdlibParameters params;

   public TDLibClient(Context app_ctx, ApiHandler apiHandler, ClientHandler handler) {
      this.apiHandler = apiHandler;
      this.handler = handler;
      this.params = new TdApi.TdlibParameters();
      params.applicationVersion = BuildConfig.VERSION_NAME;
      params.deviceModel = Build.MODEL;
      params.systemVersion = Build.VERSION.RELEASE;
      APISecureStorage.loadAppToken();
      this.client = Client.create(this, null,this);
      client.send(new TdApi.SetLogVerbosityLevel(0), null);
      try {
         params.apiId = Integer.parseInt(APISecureStorage.app_id);
         params.apiHash = APISecureStorage.app_key;
         params.databaseDirectory = app_ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         params.filesDirectory = app_ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         params.systemLanguageCode = Locale.getDefault().toString();
         sendTdlibParameters();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public TDLibClient(TdApi.TdlibParameters params) {
      this.params = params;
      this.client = Client.create(this, null,this);
      sendTdlibParameters();
   }

   public void sendTdlibParameters() {
      send(new TdApi.SetTdlibParameters(params), null);
      send(new TdApi.CheckDatabaseEncryptionKey(), null);
   }

   @Override
   public void onResult(TdApi.Object object) {
      handler.onUpdate(object);
   }

   @Override
   public void onException(Throwable e) {
      e.printStackTrace();
   }

   public void send(TdApi.Function function, ApiHandler handler) {
      if(handler != null) {
         client.send(
                 function, object -> handler.onSuccess(function, object),
                 e -> handler.onFail(function, e)
         );
      } else {
         client.send(function, null);
      }
   }

   public void send(TdApi.Function function) {
      if(apiHandler != null) {
         client.send(
                 function, object -> apiHandler.onSuccess(function, object),
                 e -> apiHandler.onFail(function, e)
         );
      } else {
         client.send(function, null);
      }
   }

   public void destroy() {
      client.close();
   }

   public TdApi.Function createFunction(int constructor, Bundle params) {
      TdApi.Function function = null;
      switch (constructor) {
         case TdApi.GetChats.CONSTRUCTOR:
            function = new TdApi.GetChats();
            break;
         case TdApi.GetChat.CONSTRUCTOR:
            if(params.containsKey("chat_id"))
               function = new TdApi.GetChat(params.getLong("chat_id"));
            else
               function = new TdApi.GetChat();
            break;
         case TdApi.GetUser.CONSTRUCTOR:
            if(params.containsKey("user_id"))
               function = new TdApi.GetUser();
            else
               function = new TdApi.GetUser(params.getLong("user_id"));
            break;
         case TdApi.GetMe.CONSTRUCTOR:
            function = new TdApi.GetMe();
            break;
      }
      return function;
   }

   public interface ApiHandler {
      void onSuccess(TdApi.Function function, TdApi.Object object);
      void onFail(TdApi.Function function, Throwable throwable);
   }

   public interface ClientHandler {
      void onUpdate(TdApi.Object object);
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
