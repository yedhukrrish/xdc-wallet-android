package com.alphawallet.app.repository;

import static com.alphawallet.app.repository.EthereumNetworkBase.INFURA_DOMAIN;


import android.text.TextUtils;

import org.web3j.protocol.http.HttpService;

import okhttp3.Request;

public class HttpServiceHelper
{
    public static void addRequiredCredentials(long chainId, HttpService httpService, String klaytnKey, String infuraKey, boolean usesProductionKey)
    {
        String serviceUrl = httpService.getUrl();
       if (serviceUrl != null && usesProductionKey && serviceUrl.contains(INFURA_DOMAIN) && !TextUtils.isEmpty(infuraKey))
        {
            httpService.addHeader("Authorization", "Basic " + infuraKey);
        }
    }

    public static void addRequiredCredentials(long chainId, Request.Builder service, String klaytnKey, String infuraKey, boolean usesProductionKey, boolean isInfura)
    {
      if (isInfura && usesProductionKey && !TextUtils.isEmpty(infuraKey))
        {
            service.addHeader("Authorization", "Basic " + infuraKey);
        }
    }
}
