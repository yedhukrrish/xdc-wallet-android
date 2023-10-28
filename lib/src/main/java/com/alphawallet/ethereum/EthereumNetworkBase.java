package com.alphawallet.ethereum;

/* Weiwu 12 Jan 2020: This class eventually will replace the EthereumNetworkBase class in :app
 * one all interface methods are implemented.
 */

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class EthereumNetworkBase
{ // implements EthereumNetworkRepositoryType
    public static final long MAINNET_ID = 51;
    public static final long CLASSIC_ID = 50;


    static Map<Long, NetworkInfo> networkMap = new LinkedHashMap<Long, NetworkInfo>()
    {
        {
//            put(MAINNET_ID, new NetworkInfo("Ethereum", "ETH", MAINNET_RPC_URL, "https://etherscan.io/tx/",
//                    MAINNET_ID, false));
//            put(CLASSIC_ID, new NetworkInfo("Ethereum Classic", "ETC", CLASSIC_RPC_URL, "https://blockscout.com/etc/mainnet/tx/",
//                    CLASSIC_ID, false));
//            put(GNOSIS_ID, new NetworkInfo("Gnosis", "xDAi", XDAI_RPC_URL, "https://blockscout.com/xdai/mainnet/tx/",
//                    GNOSIS_ID, false));
//            put(GOERLI_ID, new NetworkInfo("Görli (Test)", "GÖETH", GOERLI_RPC_URL, "https://goerli.etherscan.io/tx/",
//                    GOERLI_ID, false));
//            put(BINANCE_TEST_ID, new NetworkInfo("BSC TestNet (Test)", "T-BSC", BINANCE_TEST_RPC_URL, "https://explorer.binance.org/smart-testnet/tx/",
//                    BINANCE_TEST_ID, false));
//            put(BINANCE_MAIN_ID, new NetworkInfo("Binance (BSC)", "BSC", BINANCE_MAIN_RPC_URL, "https://explorer.binance.org/smart/tx/",
//                    BINANCE_MAIN_ID, false));
//            put(HECO_ID, new NetworkInfo("Heco", "HT", HECO_RPC_URL, "https://hecoinfo.com/tx/",
//                    HECO_ID, false));
//            put(AVALANCHE_ID, new NetworkInfo("Avalanche Mainnet C-Chain", "AVAX", AVALANCHE_RPC_URL, "https://cchain.explorer.avax.network/tx/",
//                    AVALANCHE_ID, false));
//            put(FUJI_TEST_ID, new NetworkInfo("Avalanche FUJI C-Chain (Test)", "AVAX", FUJI_TEST_RPC_URL, "https://cchain.explorer.avax-test.network/tx/",
//                    FUJI_TEST_ID, false));
//
//            put(FANTOM_ID, new NetworkInfo("Fantom Opera", "FTM", FANTOM_RPC_URL, "https://ftmscan.com/tx/",
//                    FANTOM_ID, false));
//            put(FANTOM_TEST_ID, new NetworkInfo("Fantom (Test)", "FTM", FANTOM_TEST_RPC_URL, "https://explorer.testnet.fantom.network/tx/",
//                    FANTOM_TEST_ID, false));
//
//            put(POLYGON_ID, new NetworkInfo("Polygon", "POLY", MATIC_RPC_URL, "https://polygonscan.com/tx/",
//                    POLYGON_ID, false));
//            put(POLYGON_TEST_ID, new NetworkInfo("Mumbai (Test)", "POLY", MUMBAI_TEST_RPC_URL, "https://mumbai.polygonscan.com/tx/",
//                    POLYGON_TEST_ID, false));
//
//            put(OPTIMISTIC_MAIN_ID, new NetworkInfo("Optimistic", "ETH", OPTIMISTIC_MAIN_FALLBACK_URL, "https://optimistic.etherscan.io/tx/",
//                    OPTIMISTIC_MAIN_ID, false));
//            put(CRONOS_MAIN_ID, new NetworkInfo("Cronos (Beta)", "CRO", CRONOS_MAIN_RPC_URL, "https://cronoscan.com/tx", CRONOS_MAIN_ID, false));
//            put(CRONOS_TEST_ID, new NetworkInfo("Cronos (Test)", "tCRO", CRONOS_TEST_URL, "https://testnet.cronoscan.com/tx/", CRONOS_TEST_ID, false));
//            put(ARBITRUM_MAIN_ID, new NetworkInfo("Arbitrum One", "AETH", ARBITRUM_RPC_URL, "https://arbiscan.io/tx/",
//                    ARBITRUM_MAIN_ID, false));
//
//            put(PALM_ID, new NetworkInfo("PALM", "PALM", PALM_RPC_URL, "https://explorer.palm.io/tx/",
//                    PALM_ID, false));
//            put(PALM_TEST_ID, new NetworkInfo("PALM (Test)", "PALM", PALM_TEST_RPC_URL, "https://explorer.palm-uat.xyz/tx/",
//                    PALM_TEST_ID, false));
//            put(KLAYTN_ID, new NetworkInfo("Klaytn Cypress", "KLAY", KLAYTN_RPC, "https://scope.klaytn.com/tx/",
//                    KLAYTN_ID, false));
//            put(KLAYTN_BAOBAB_ID, new NetworkInfo("Klaytn Baobab (Test)", "KLAY", KLAYTN_BAOBAB_RPC, "https://baobab.scope.klaytn.com/tx/",
//                    KLAYTN_BAOBAB_ID, false));
//            put(AURORA_MAINNET_ID, new NetworkInfo("Aurora", "ETH", AURORA_MAINNET_RPC_URL, "https://aurorascan.dev/tx/",
//                    AURORA_MAINNET_ID, false));
//            put(AURORA_TESTNET_ID, new NetworkInfo("Aurora (Test)", "ETH", AURORA_TESTNET_RPC_URL, "https://testnet.aurorascan.dev/tx/",
//                    AURORA_TESTNET_ID, false));
//
//            put(MILKOMEDA_C1_ID, new NetworkInfo("Milkomeda Cardano", "milkADA", MILKOMEDA_C1_RPC, "https://explorer-mainnet-cardano-evm.c1.milkomeda.com/tx/",
//                    MILKOMEDA_C1_ID, false));
//            put(MILKOMEDA_C1_TEST_ID, new NetworkInfo("Milkomeda Cardano (Test)", "milktADA", MILKOMEDA_C1_TEST_RPC, "https://explorer-devnet-cardano-evm.c1.milkomeda.com/tx/",
//                    MILKOMEDA_C1_TEST_ID, false));
//            put(SEPOLIA_TESTNET_ID, new NetworkInfo("Sepolia (Test)", "ETH", SEPOLIA_TESTNET_RPC_URL, "https://sepolia.etherscan.io/tx/",
//                    SEPOLIA_TESTNET_ID, false));
//            put(OPTIMISM_GOERLI_TEST_ID, new NetworkInfo("Optimism Goerli (Test)", "ETH", OPTIMISM_GOERLI_TESTNET_FALLBACK_RPC_URL, "https://blockscout.com/optimism/goerli/tx/",
//                    OPTIMISM_GOERLI_TEST_ID, false));
//            put(ARBITRUM_GOERLI_TEST_ID, new NetworkInfo("Arbitrum Goerli (Test)", "AGOR", OPTIMISM_GOERLI_TESTNET_FALLBACK_RPC_URL, "https://goerli-rollup-explorer.arbitrum.io/tx/",
//                    ARBITRUM_GOERLI_TEST_ID, false));
//            put(IOTEX_MAINNET_ID, new NetworkInfo("IoTeX", "IOTX", IOTEX_MAINNET_RPC_URL, "https://iotexscan.io/tx/",
//                    IOTEX_MAINNET_ID, false));
//            put(IOTEX_TESTNET_ID, new NetworkInfo("IoTeX (Test)", "IOTX", IOTEX_TESTNET_RPC_URL, "https://testnet.iotexscan.io/tx/",
//                    IOTEX_TESTNET_ID, false));
//            put(OKX_ID, new NetworkInfo("OKXChain Mainnet", "OKT", OKX_RPC_URL, "https://www.oklink.com/en/okc",
//                OKX_ID, false));
//            put(ROOTSTOCK_MAINNET_ID, new NetworkInfo("Rootstock", "RBTC", ROOTSTOCK_MAINNET_RPC_URL, "https://blockscout.com/rsk/mainnet/tx/",
//                    ROOTSTOCK_MAINNET_ID, false));
//            put(ROOTSTOCK_TESTNET_ID, new NetworkInfo("Rootstock (Test)", "tBTC", ROOTSTOCK_TESTNET_RPC_URL, "",
//                    ROOTSTOCK_TESTNET_ID, false));
//
//            put(LINEA_ID, new NetworkInfo("Linea", "ETH", LINEA_FREE_RPC, "https://lineascan.build/tx/",
//                    LINEA_ID, false));
//            put(LINEA_TEST_ID, new NetworkInfo("Linea (Test)", "ETH", LINEA_TEST_FREE_RPC, "https://goerli.lineascan.build/tx/",
//                    LINEA_TEST_ID, false));

        }
    };

    public static NetworkInfo getNetworkByChain(long chainId)
    {
        return networkMap.get(chainId);
    }

    public static String getShortChainName(long chainId)
    {
        NetworkInfo info = networkMap.get(chainId);
        if (info != null)
        {
            String shortName = info.name;
            int index = shortName.indexOf(" (Test)");
            if (index > 0) shortName = info.name.substring(0, index);
            if (shortName.length() > networkMap.get(CLASSIC_ID).name.length()) //shave off the last word
            {
                shortName = shortName.substring(0, shortName.lastIndexOf(" "));
            }
            return shortName;
        }
        else
        {
            return networkMap.get(MAINNET_ID).name;
        }
    }

    public static String getChainSymbol(long chainId)
    {
        NetworkInfo info = networkMap.get(chainId);
        if (info != null)
        {
            return info.symbol;
        }
        else
        {
            return networkMap.get(MAINNET_ID).symbol;
        }
    }
}
