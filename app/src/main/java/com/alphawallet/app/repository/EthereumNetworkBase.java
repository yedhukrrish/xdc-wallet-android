package com.alphawallet.app.repository;

/* Please don't add import android at this point. Later this file will be shared
 * between projects including non-Android projects */

import static com.alphawallet.app.entity.EventSync.BLOCK_SEARCH_INTERVAL;
import static com.alphawallet.app.entity.EventSync.OKX_BLOCK_SEARCH_INTERVAL;
import static com.alphawallet.app.entity.EventSync.POLYGON_BLOCK_SEARCH_INTERVAL;
import static com.alphawallet.app.util.Utils.isValidUrl;

import static com.alphawallet.ethereum.EthereumNetworkBase.CLASSIC_ID;
import static com.alphawallet.ethereum.EthereumNetworkBase.MAINNET_ID;

import android.text.TextUtils;
import android.util.LongSparseArray;

import com.alphawallet.app.BuildConfig;
import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.ContractLocator;
import com.alphawallet.app.entity.ContractType;
import com.alphawallet.app.entity.CustomViewSettings;
import com.alphawallet.app.entity.NetworkInfo;
import com.alphawallet.app.entity.Wallet;
import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.entity.tokens.TokenInfo;
import com.alphawallet.app.util.Utils;
import com.alphawallet.token.entity.ChainSpec;
import com.google.gson.Gson;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Single;

public abstract class EthereumNetworkBase implements EthereumNetworkRepositoryType
{
    public static final String COVALENT = "[COVALENT]";

    private static final String GAS_API = "module=gastracker&action=gasoracle";

    public static final String DEFAULT_INFURA_KEY = "da3717f25f824cc1baa32d812386d93f";
    /* constructing URLs from BuildConfig. In the below area you will see hardcoded key like da3717...
       These hardcoded keys are fallbacks used by AlphaWallet forks.

       Also note: If you are running your own node and wish to use that; currently it must be hardcoded here
       If you wish your node to be the primary node that AW checks then replace the relevant ..._RPC_URL below
       If you wish your node to be the fallback, tried in case the primary times out then add/replace in ..._FALLBACK_RPC_URL list
     */

    private static final KeyProvider keyProvider = KeyProviderFactory.get();
    public static final boolean usesProductionKey = !keyProvider.getInfuraKey().equals(DEFAULT_INFURA_KEY);

    public static final String FREE_MAINNET_RPC_URL = "https://rpc.apothem.network/";

    public static final String MAINNET_RPC_URL = "https://rpc.apothem.network/";

    // Use the "Free" routes as backup in order to diversify node usage; to avoid single point of failure
    public static final String MAINNET_FALLBACK_RPC_URL = usesProductionKey ? FREE_MAINNET_RPC_URL : "https://mainnet.infura.io/v3/" + keyProvider.getSecondaryInfuraKey();

    //Note that AlphaWallet now uses a double node configuration. See class AWHttpService comment 'try primary node'.
    //If you supply a main RPC and secondary it will try the secondary if the primary node times out after 10 seconds.
    //See the declaration of NetworkInfo - it has a member backupNodeUrl. Put your secondary node here.

    public static final String CLASSIC_RPC_URL = "https://rpc.ankr.com/xdc";

    //All chains that have fiat/real value (not testnet) must be put here
    //Note: This list also determines the order of display for main net chains in the wallet.
    //If your wallet prioritises xDai for example, you may want to move the XDAI_ID to the front of this list,
    //Then xDai would appear as the first token at the top of the wallet
    private static final List<Long> hasValue = new ArrayList<>(Arrays.asList(
            MAINNET_ID,  CLASSIC_ID));

    private static final List<Long> testnetList = new ArrayList<>(Arrays.asList());

    private static final List<Long> deprecatedNetworkList = new ArrayList<>(Arrays.asList(
            // Add deprecated testnet IDs here
    ));

    private static final String INFURA_ENDPOINT = ".infura.io/v3/";

    @Override
    public String getDappBrowserRPC(long chainId)
    {
        NetworkInfo info = getNetworkByChain(chainId);

        if (info == null)
        {
            return "";
        }

        int index = info.rpcServerUrl.indexOf(INFURA_ENDPOINT);
        if (index > 0)
        {
            return info.rpcServerUrl.substring(0, index + INFURA_ENDPOINT.length()) + keyProvider.getTertiaryInfuraKey();
        }
        else
        {
            return info.backupNodeUrl != null ? info.backupNodeUrl : info.rpcServerUrl;
        }
    }

    public static boolean isInfura(String rpcServerUrl)
    {
        return rpcServerUrl.contains(INFURA_ENDPOINT);
    }

    // for reset built-in network
    private static final LongSparseArray<NetworkInfo> builtinNetworkMap = new LongSparseArray<NetworkInfo>()
    {
        {
            put(MAINNET_ID,new NetworkInfo(C.CLASSIC_NETWORK_NAME, C.ETH_SYMBOL,
                    MAINNET_RPC_URL,
                    "https://xdc.blocksscan.io", MAINNET_ID,
                    MAINNET_RPC_URL, "https://xdc.blocksscan.io/api?"));
            put(CLASSIC_ID, new NetworkInfo(C.CLASSIC_NETWORK_NAME, C.ETC_SYMBOL,
                    CLASSIC_RPC_URL,
                    "https://xdc.blocksscan.io", CLASSIC_ID, CLASSIC_RPC_URL,
                    "https://xdc.blocksscan.io/api?"));
        }
    };

    //List of network details. Note, the advantage of using LongSparseArray is efficiency and also
    //the entries are automatically sorted into numerical order
    private static final LongSparseArray<NetworkInfo> networkMap = builtinNetworkMap.clone();

    private static final LongSparseArray<Integer> chainLogos = new LongSparseArray<Integer>()
    {
        {
            put(MAINNET_ID, R.drawable.xdcicon);
            put(CLASSIC_ID, R.drawable.xdcicon); //classic_logo

        }
    };

    private static final LongSparseArray<Integer> smallChainLogos = new LongSparseArray<Integer>()
    {
        {
            put(MAINNET_ID, R.drawable.xdcicon);
            put(CLASSIC_ID, R.drawable.xdcicon);

        }
    };

    private static final LongSparseArray<Integer> chainColours = new LongSparseArray<Integer>()
    {
        {
            put(MAINNET_ID, R.color.mainnet);
            put(CLASSIC_ID, R.color.classic);

        }
    };

    //Does the chain have a gas oracle?
    //Add it to this list here if so. Note that so far, all gas oracles follow the same format:
    //  <etherscanAPI from the above list> + GAS_API
    //If the gas oracle you're adding doesn't follow this spec then you'll have to change the getGasOracle method
    private static final List<Long> hasGasOracleAPI = Arrays.asList(MAINNET_ID);
    private static final List<Long> hasBlockNativeGasOracleAPI = Arrays.asList(MAINNET_ID);
    //These chains don't allow custom gas
    private static final List<Long> hasOpenSeaAPI = Arrays.asList(MAINNET_ID);

    private static final LongSparseArray<BigInteger> blockGasLimit = new LongSparseArray<BigInteger>()
    {
        {
            put(MAINNET_ID, BigInteger.valueOf(C.GAS_LIMIT_MAX));
        }
    };

    public static String getGasOracle(long chainId)
    {
        if (hasGasOracleAPI.contains(chainId) && networkMap.indexOfKey(chainId) >= 0)
        {
            return networkMap.get(chainId).etherscanAPI + GAS_API;
        }
        else
        {
            return "";
        }
    }

    private static final String BLOCKNATIVE_GAS_API = "https://api.blocknative.com/gasprices/blockprices?chainid=";

    public static String getBlockNativeOracle(long chainId)
    {
        if (hasBlockNativeGasOracleAPI.contains(chainId) && networkMap.indexOfKey(chainId) >= 0)
        {
            return BLOCKNATIVE_GAS_API + chainId;
        }
        else
        {
            return "";
        }
    }

    /**
     * This function determines the order in which chains appear in the main wallet view
     *
     * TODO: Modify so that custom networks with value appear between the 'hasValue' and 'testnetList' chains
     *
     * @param chainId
     * @return
     */
    public static int getChainOrdinal(long chainId)
    {
        if (hasValue.contains(chainId))
        {
            return hasValue.indexOf(chainId);
        }
        else if (testnetList.contains(chainId))
        {
            return hasValue.size() + testnetList.indexOf(chainId);
        }
        else
        {
            return hasValue.size() + testnetList.size() + (int) chainId % 500;
        }
    }

    public static final int INFURA_BATCH_LIMIT = 512;
    public static final String INFURA_DOMAIN = "infura.io";

    //TODO: Refactor when we bump the version of java to allow switch on Long (Finally!!)
    //Also TODO: add a test to check these batch limits of each chain we support
    private static int batchProcessingLimit(long chainId)
    {
        NetworkInfo info = builtinNetworkMap.get(chainId);
        if (info.rpcServerUrl.contains(INFURA_DOMAIN)) //infura supported chains can handle tx batches of 1000 and up
        {
            return INFURA_BATCH_LIMIT;
        }
        else if (info.rpcServerUrl.contains("klaytn") || info.rpcServerUrl.contains("rpc.ankr.com"))
        {
            return 0;
        }
        else if (info.rpcServerUrl.contains("cronos.org"))
        {
            return 5; //TODO: Check limit
        }
        else
        {
            return 32;
        }
    }

    private static final LongSparseArray<Integer> batchProcessingLimitMap = new LongSparseArray<>();

    //Init the batch limits
    private static void setBatchProcessingLimits()
    {
        for (int i = 0; i < builtinNetworkMap.size(); i++)
        {
            NetworkInfo info = builtinNetworkMap.valueAt(i);
            batchProcessingLimitMap.put(info.chainId, batchProcessingLimit(info.chainId));
        }
    }

    public static int getBatchProcessingLimit(long chainId)
    {
        if (batchProcessingLimitMap.size() == 0) setBatchProcessingLimits(); //If batch limits not set, init them and proceed
        return batchProcessingLimitMap.get(chainId, 0); //default to zero / no batching
    }

    @Override
    public boolean hasLockedGas(long chainId)
    {
        return false;
    }

    @Override
    public boolean hasBlockNativeGasAPI(long chainId)
    {
        return hasBlockNativeGasOracleAPI.contains(chainId);
    }

    static final Map<Long, String> addressOverride = new HashMap<Long, String>()
    {
        {
            put(MAINNET_ID, "0x4200000000000000000000000000000000000006");
        }
    };

    final PreferenceRepositoryType preferences;
    private final Set<OnNetworkChangeListener> onNetworkChangedListeners = new HashSet<>();
    final boolean useTestNets;
    final NetworkInfo[] additionalNetworks;


    static class CustomNetworks
    {
        private ArrayList<NetworkInfo> list = new ArrayList<>();
        private Map<Long, Boolean> mapToTestNet = new HashMap<>();
        final transient private PreferenceRepositoryType preferences;

        public CustomNetworks(PreferenceRepositoryType preferences)
        {
            this.preferences = preferences;
            restore();
        }

        public void restore()
        {
            String networks = preferences.getCustomRPCNetworks();
            if (!TextUtils.isEmpty(networks))
            {
                CustomNetworks cn = new Gson().fromJson(networks, CustomNetworks.class);
                this.list = cn.list;
                this.mapToTestNet = cn.mapToTestNet;
                if (list == null)
                {
                    return;
                }

                checkCustomNetworkSetting();

                for (NetworkInfo info : list)
                {
                    if (!isValidUrl(info.rpcServerUrl)) //ensure RPC doesn't contain malicious code
                    {
                        continue;
                    }

                    networkMap.put(info.chainId, info);
                    Boolean value = mapToTestNet.get(info.chainId);
                    boolean isTestnet = value != null && value;
                    if (!isTestnet && !hasValue.contains(info.chainId))
                    {
                        hasValue.add(info.chainId);
                    }
                    else if (isTestnet && !testnetList.contains(info.chainId))
                    {
                        testnetList.add(info.chainId);
                    }
                }
            }
        }

        private void checkCustomNetworkSetting()
        {
            if (list.size() > 0 && !list.get(0).isCustom)
            { //need to update the list
                List<NetworkInfo> copyList = new ArrayList<>(list);
                list.clear();
                for (NetworkInfo n : copyList)
                {
                    boolean isCustom = builtinNetworkMap.indexOfKey(n.chainId) == -1;
                    NetworkInfo newInfo = new NetworkInfo(n.name, n.symbol, n.rpcServerUrl, n.etherscanUrl, n.chainId, n.backupNodeUrl, n.etherscanAPI, isCustom);
                    list.add(newInfo);
                }
                //record back
                preferences.setCustomRPCNetworks(new Gson().toJson(this));
            }
        }

        public void save(NetworkInfo info, boolean isTestnet, Long oldChainId)
        {
            if (oldChainId != null)
            {
                updateNetwork(info, isTestnet, oldChainId);
            }
            else
            {
                addNetwork(info, isTestnet);
            }

            String networks = new Gson().toJson(this);
            preferences.setCustomRPCNetworks(networks);
        }

        private void updateNetwork(NetworkInfo info, boolean isTestnet, long oldChainId)
        {
            removeNetwork(oldChainId);
            list.add(info);
            if (!isTestnet)
            {
                hasValue.add(info.chainId);
            }
            else
            {
                testnetList.add(info.chainId);
            }
            mapToTestNet.put(info.chainId, isTestnet);
            networkMap.put(info.chainId, info);
        }

        private void addNetwork(NetworkInfo info, boolean isTestnet)
        {
            list.add(info);
            if (!isTestnet)
            {
                hasValue.add(info.chainId);
            }
            else
            {
                testnetList.add(info.chainId);
            }
            mapToTestNet.put(info.chainId, isTestnet);
            networkMap.put(info.chainId, info);
        }

        public void remove(long chainId)
        {
            removeNetwork(chainId);

            String networks = new Gson().toJson(this);
            preferences.setCustomRPCNetworks(networks);
        }

        private void removeNetwork(long chainId)
        {
            for (NetworkInfo in : list)
            {
                if (in.chainId == chainId)
                {
                    list.remove(in);
                    break;
                }
            }
            hasValue.remove(chainId);
            mapToTestNet.remove(chainId);
            networkMap.remove(chainId);
        }
    }

    private static CustomNetworks customNetworks;

    EthereumNetworkBase(PreferenceRepositoryType preferenceRepository, NetworkInfo[] additionalNetworks, boolean useTestNets)
    {
        this.preferences = preferenceRepository;
        this.additionalNetworks = additionalNetworks;
        this.useTestNets = useTestNets;

        customNetworks = new CustomNetworks(this.preferences);
    }

    private void addNetworks(NetworkInfo[] networks, List<NetworkInfo> result, boolean withValue)
    {
        for (NetworkInfo network : networks)
        {
            if (EthereumNetworkRepository.hasRealValue(network.chainId) == withValue
                    && !result.contains(network))
            {
                result.add(network);
            }
        }
    }

    private void addNetworks(List<NetworkInfo> result, boolean withValue)
    {
        if (withValue)
        {
            for (long networkId : hasValue)
            {
                if (!deprecatedNetworkList.contains(networkId))
                {
                    result.add(networkMap.get(networkId));
                }
            }

            for (long networkId : hasValue)
            {
                if (deprecatedNetworkList.contains(networkId))
                {
                    result.add(networkMap.get(networkId));
                }
            }
        }
        else
        {
            for (long networkId : testnetList)
            {
                if (!deprecatedNetworkList.contains(networkId))
                {
                    result.add(networkMap.get(networkId));
                }
            }

            for (long networkId : testnetList)
            {
                if (deprecatedNetworkList.contains(networkId))
                {
                    result.add(networkMap.get(networkId));
                }
            }
        }
    }

    public static String getChainOverrideAddress(long chainId)
    {
        return addressOverride.containsKey(chainId) ? addressOverride.get(chainId) : "";
    }

    @Override
    public String getNameById(long chainId)
    {
        if (networkMap.indexOfKey(chainId) >= 0) return networkMap.get(chainId).name;
        else return "Unknown: " + chainId;
    }

    @Override
    public NetworkInfo getActiveBrowserNetwork()
    {
        long activeNetwork = preferences.getActiveBrowserNetwork();
        return networkMap.get(activeNetwork);
    }

    @Override
    public NetworkInfo getNetworkByChain(long chainId)
    {
        return networkMap.get(chainId);
    }

    // Static variant to replace static in the other EthereumNetworkBase
    public static NetworkInfo getNetwork(long chainId)
    {
        return networkMap.get(chainId);
    }

    // fetches the last transaction nonce; if it's identical to the last used one then increment by one
    // to ensure we don't get transaction replacement
    @Override
    public Single<BigInteger> getLastTransactionNonce(Web3j web3j, String walletAddress)
    {
        return Single.fromCallable(() ->
        {
            try
            {
                EthGetTransactionCount ethGetTransactionCount = web3j
                        .ethGetTransactionCount(walletAddress, DefaultBlockParameterName.PENDING)
                        .send();
                return ethGetTransactionCount.getTransactionCount();
            }
            catch (Exception e)
            {
                return BigInteger.ZERO;
            }
        });
    }

    @Override
    public List<Long> getFilterNetworkList()
    {
        return getSelectedFilters();
    }

    @Override
    public List<Long> getSelectedFilters()
    {
        String filterList = preferences.getNetworkFilterList();
        List<Long> storedIds = Utils.longListToArray(filterList);
        List<Long> selectedIds = new ArrayList<>();

        for (Long networkId : storedIds)
        {
            NetworkInfo check = networkMap.get(networkId);
            if (check != null) selectedIds.add(networkId);
        }

        if (selectedIds.size() == 0)
        {
            selectedIds.add(getDefaultNetwork());
        }

        return selectedIds;
    }

    @Override
    public Long getDefaultNetwork()
    {
        return CustomViewSettings.primaryChain;
    }

    @Override
    public void setFilterNetworkList(Long[] networkList)
    {
        String store = Utils.longArrayToString(networkList);
        preferences.setNetworkFilterList(store);
    }

    @Override
    public void setActiveBrowserNetwork(NetworkInfo networkInfo)
    {
        if (networkInfo != null)
        {
            preferences.setActiveBrowserNetwork(networkInfo.chainId);
            for (OnNetworkChangeListener listener : onNetworkChangedListeners)
            {
                listener.onNetworkChanged(networkInfo);
            }
        }
        else
        {
            preferences.setActiveBrowserNetwork(0);
        }
    }

    @Override
    public NetworkInfo[] getAvailableNetworkList()
    {
        //construct on demand, and give in order
        /* merging static compile time network list with runtime network list */
        List<NetworkInfo> networks = new ArrayList<>();

        addNetworks(additionalNetworks, networks, true);
        addNetworks(networks, true);
        /* the order is passed to the user interface. So if a user has a token on one
         * of the additionalNetworks, the same token on DEFAULT_NETWORKS, and on a few
         * test nets, they are displayed by that order.
         */
        addNetworks(additionalNetworks, networks, false);
        if (useTestNets) addNetworks(networks, false);
        return networks.toArray(new NetworkInfo[0]);
    }

    @Override
    public NetworkInfo[] getAllActiveNetworks()
    {
        NetworkInfo[] allNetworks = getAvailableNetworkList();
        List<NetworkInfo> networks = new ArrayList<>();
        addNetworks(allNetworks, networks, true);
        return networks.toArray(new NetworkInfo[0]);
    }

    @Override
    public void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged)
    {
        onNetworkChangedListeners.add(onNetworkChanged);
    }

    public static boolean hasRealValue(long chainId)
    {
        return hasValue.contains(chainId);
    }

    public static List<Long> getAllMainNetworks()
    {
        return hasValue;
    }

    public static String getSecondaryNodeURL(long networkId)
    {
        NetworkInfo info = networkMap.get(networkId);
        if (info != null)
        {
            return info.backupNodeUrl;
        }
        else
        {
            return "";
        }
    }

    //TODO: Fold this into file and add to database
    public static int getChainLogo(long networkId)
    {
        if (chainLogos.indexOfKey(networkId) >= 0)
        {
            return chainLogos.get(networkId);
        }
        else
        {
            return R.drawable.ic_ethereum_generic;
        }
    }

    public static int getSmallChainLogo(long networkId)
    {
        if (smallChainLogos.indexOfKey(networkId) >= 0)
        {
            return smallChainLogos.get(networkId);
        }
        else
        {
            return getChainLogo(networkId);
        }
    }

    public static int getChainColour(long chainId)
    {
        if (chainColours.indexOfKey(chainId) >= 0)
        {
            return chainColours.get(chainId);
        }
        else
        {
            return R.color.text_primary;
        }
    }

    public static BigInteger getMaxGasLimit(long chainId)
    {
        return blockGasLimit.get(chainId, blockGasLimit.get(MAINNET_ID));
    }

    public static String getNodeURLByNetworkId(long networkId)
    {
        NetworkInfo info = networkMap.get(networkId);
        if (info != null)
        {
            return info.rpcServerUrl;
        }
        else
        {
            return MAINNET_RPC_URL;
        }
    }

    /**
     * This is used so as not to leak API credentials to web3; XInfuraAPI is the backup API key checked into github
     *
     * @param chainId
     * @return
     */
    public static String getDefaultNodeURL(long chainId)
    {
        NetworkInfo info = networkMap.get(chainId);

        if (info == null)
        {
            return "";
        }

        int index = info.rpcServerUrl.indexOf(INFURA_ENDPOINT);
        if (index > 0)
        {
            return info.rpcServerUrl.substring(0, index + INFURA_ENDPOINT.length()) + keyProvider.getTertiaryInfuraKey();
        }
        else
        {
            return info.backupNodeUrl != null ? info.backupNodeUrl : info.rpcServerUrl;
        }
    }

    public static long getNetworkIdFromName(String name)
    {
        if (!TextUtils.isEmpty(name))
        {
            for (int i = 0; i < networkMap.size(); i++)
            {
                if (name.equals(networkMap.valueAt(i).name))
                {
                    return networkMap.valueAt(i).chainId;
                }
            }
        }
        return 0;
    }

    //Note: this is used by chains which have a fixed, invariable gas price.
    //      it mainly only applies to private or custom chains, eg a Kaleido based chain
    //      public chains will almost never use this.
    public static boolean hasGasOverride(long chainId)
    {
        return false;
    }

    public static boolean hasOpenseaAPI(long chainId)
    {
        return hasOpenSeaAPI.contains(chainId);
    }

    public static BigInteger gasOverrideValue(long chainId)
    {
        return BigInteger.valueOf(1);
    }

    public static List<ChainSpec> extraChains()
    {
        return null;
    }

    public static List<Long> addDefaultNetworks()
    {
        return CustomViewSettings.alwaysVisibleChains;
    }

    public static ContractLocator getOverrideToken()
    {
        return new ContractLocator("", CustomViewSettings.primaryChain, ContractType.ETHEREUM);
    }

    @Override
    public boolean isChainContract(long chainId, String address)
    {
        return (addressOverride.containsKey(chainId) && address.equalsIgnoreCase(addressOverride.get(chainId)));
    }

    public static boolean isPriorityToken(Token token)
    {
        return false;
    }

    public static long getPriorityOverride(Token token)
    {
        if (token.isEthereum()) return token.tokenInfo.chainId + 1;
        else return 0;
    }

    public static int decimalOverride(String address, long chainId)
    {
        return 0;
    }

    public Token getBlankOverrideToken(NetworkInfo networkInfo)
    {
        return createCurrencyToken(networkInfo);
    }

    public Single<Token[]> getBlankOverrideTokens(Wallet wallet)
    {
        return Single.fromCallable(() ->
        {
            if (getBlankOverrideToken() == null)
            {
                return new Token[0];
            }
            else
            {
                Token[] tokens = new Token[1];
                tokens[0] = getBlankOverrideToken();
                tokens[0].setTokenWallet(wallet.address);
                return tokens;
            }
        });
    }

    private static Token createCurrencyToken(NetworkInfo network)
    {
        TokenInfo tokenInfo = new TokenInfo(Address.DEFAULT.toString(), network.name, network.symbol, 18, true, network.chainId);
        BigDecimal balance = BigDecimal.ZERO;
        Token eth = new Token(tokenInfo, balance, 0, network.getShortName(), ContractType.ETHEREUM); //create with zero time index to ensure it's updated immediately
        eth.setTokenWallet(Address.DEFAULT.toString());
        eth.setIsEthereum();
        eth.pendingBalance = balance;
        return eth;
    }

    public Token getBlankOverrideToken()
    {
        return null;
    }

    public String getCurrentWalletAddress()
    {
        return preferences.getCurrentWalletAddress();
    }

    public boolean hasSetNetworkFilters()
    {
        return preferences.hasSetNetworkFilters();
    }

    public void setHasSetNetworkFilters()
    {
        preferences.setHasSetNetworkFilters();
    }

    public void saveCustomRPCNetwork(String networkName, String rpcUrl, long chainId, String symbol, String blockExplorerUrl, String explorerApiUrl, boolean isTestnet, Long oldChainId)
    {

        NetworkInfo builtInNetwork = builtinNetworkMap.get(chainId);
        boolean isCustom = builtInNetwork == null;
        NetworkInfo info = new NetworkInfo(networkName, symbol, rpcUrl, blockExplorerUrl, chainId, isCustom ? null : builtInNetwork.backupNodeUrl, explorerApiUrl, isCustom);
        customNetworks.save(info, isTestnet, oldChainId);
    }

    public void removeCustomRPCNetwork(long chainId)
    {
        customNetworks.remove(chainId);
    }

    public static NetworkInfo getNetworkInfo(long chainId)
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
            // Unsupported network: method caller should handle this scenario
            return "";
        }
    }

    public static boolean isChainSupported(long chainId)
    {
        return networkMap.get(chainId) != null;
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

    public static boolean isEventBlockLimitEnforced(long chainId)
    {
        if (true)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static BigInteger getMaxEventFetch(long chainId)
    {
        if (true)
        {
            return BigInteger.valueOf(POLYGON_BLOCK_SEARCH_INTERVAL);
        }
        else if (true)
        {
            return BigInteger.valueOf(OKX_BLOCK_SEARCH_INTERVAL);
        }
        else
        {
            return BigInteger.valueOf(BLOCK_SEARCH_INTERVAL);
        }
    }

    public static String getNodeURLForEvents(long chainId)
    {
        {
            return getNodeURLByNetworkId(chainId);
        }
    }

    @Override
    public NetworkInfo getBuiltInNetwork(long chainId)
    {
        return builtinNetworkMap.get(chainId);
    }

    public static boolean isNetworkDeprecated(long chainId)
    {
        return deprecatedNetworkList.contains(chainId);
    }

    @Override
    public void commitPrefs()
    {
        preferences.commit();
    }

    public static List<Long> getAllNetworks()
    {
        ArrayList<Long> list = new ArrayList<>();
        list.addAll(hasValue);
        list.addAll(testnetList);
        return list;
    }
}
