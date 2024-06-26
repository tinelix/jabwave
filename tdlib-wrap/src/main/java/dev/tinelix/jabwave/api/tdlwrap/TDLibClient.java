package dev.tinelix.jabwave.api.tdlwrap;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.Nullable;
import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.SecureStorage;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class TDLibClient extends BaseClient implements Client.ResultHandler, Client.ExceptionHandler {

   private final Client client;
   public ApiHandler apiHandler;
   private ClientHandler handler;
   private TdApi.TdlibParameters params;

   public static final String TELEGRAM_SERV_TAG = "TelegramService";

   @SuppressWarnings("ConstantConditions")
   public TDLibClient(Context ctx,
                      ApiHandler apiHandler,
                      ClientHandler handler,
                      SecureStorage storage,
                      ClientIdentityParams params
   ) {
      super(ctx, true, "Telegram");
      this.apiHandler = apiHandler;
      this.handler = handler;
      this.params = new TdApi.TdlibParameters();
      this.params.applicationVersion = params.getClientIdentity().get("client_version");
      this.params.deviceModel = Build.MODEL;
      this.params.systemVersion = Build.VERSION.RELEASE;
      HashMap<String, Object> api_map = storage.loadAppToken();
      this.client = Client.create(this, null,this);
      client.send(new TdApi.SetLogVerbosityLevel(0), null);
      try {
         this.params.apiId = api_map.containsKey("app_id") ? (int) api_map.get("app_id") : 0;
         this.params.apiHash = (String) api_map.get("app_hash");
         this.params.databaseDirectory = ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         this.params.filesDirectory = ctx.getExternalFilesDir(null).getAbsolutePath() + "/";
         this.params.systemLanguageCode = Locale.getDefault().toString();
         sendTdlibParameters();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public TDLibClient(Context ctx, TdApi.TdlibParameters params) {
      super(ctx, true, "Telegram");
      this.params = params;
      this.client = Client.create(this, null,this);
      sendTdlibParameters();
   }

   public void sendTdlibParameters() {
      OnClientAPIResultListener listener = new OnClientAPIResultListener() {
         @Override
         public boolean onSuccess(HashMap<String, Object> map) {
            return false;
         }

         @Override
         public boolean onFail(HashMap<String, Object> map, Throwable t) {
            return false;
         }
      };
      send(new TdApi.SetTdlibParameters(params), listener);
      send(new TdApi.CheckDatabaseEncryptionKey(), listener);
   }

   @Override
   public void onResult(TdApi.Object object) {
      handler.onUpdate(object);
   }

   @Override
   public void onException(Throwable e) {
      e.printStackTrace();
   }

   @Override
   public void send(Object function, OnClientAPIResultListener listener) {
      HashMap<String, Object> map = new HashMap<>();
      map.put("network_name", network_name);
      map.put("function", function);
      if(listener != null) {
         client.send(
                 ((TdApi.Function) function),
                 object -> {
                     if(object instanceof TdApi.Error) {
                        onException(new Throwable(((TdApi.Error) object).message));
                        return;
                     }
                     map.put("result", object);
                     listener.onSuccess(map);
                 },
                 e -> {
                     e.printStackTrace();
                     listener.onFail(map, e);
                 }
         );
      } else {
         client.send(((TdApi.Function) function), object -> {});
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

   public void downloadFile(TdApi.File file, OnClientAPIResultListener listener) {

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
