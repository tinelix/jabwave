package dev.tinelix.jabwave.telegram.api;


import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.JabwaveApp;

public class TDLibClient implements Client.ResultHandler, Client.ExceptionHandler {

   private final Client client;
   private TdApi.TdlibParameters params;

   public TDLibClient() {
      this.params = new TdApi.TdlibParameters();
      try {
         if (!BuildConfig.APP_TOKEN.equals("UNKNOWN.UNKNOWN")) {
            params.apiId = Integer.parseInt(BuildConfig.APP_TOKEN.split("\\.")[0]);
            params.apiHash = BuildConfig.APP_TOKEN.split("\\.")[1];
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      this.client = Client.create(this, null,this);
   }

   public TDLibClient(TdApi.TdlibParameters params) {
      this.params = params;
      this.client = Client.create(this, null,this);
   }

   @Override
   public void onResult(TdApi.Object object) {
      Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
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
      client.send(
              function, handler::onSuccess,
              handler::onFail
      );
   }

   public interface ApiHandler {
      void onSuccess(TdApi.Object object);
      void onFail(Throwable throwable);
   }
}
