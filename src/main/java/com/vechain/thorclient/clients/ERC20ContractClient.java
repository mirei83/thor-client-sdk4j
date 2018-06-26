package com.vechain.thorclient.clients;

import com.vechain.thorclient.core.model.blockchain.ContractCall;
import com.vechain.thorclient.core.model.blockchain.ContractCallResult;
import com.vechain.thorclient.core.model.blockchain.TransferResult;
import com.vechain.thorclient.core.model.clients.*;
import com.vechain.thorclient.core.model.clients.base.AbiDefinition;
import com.vechain.thorclient.core.model.exception.ClientArgumentException;
import com.vechain.thorclient.core.model.exception.ClientIOException;
import com.vechain.thorclient.utils.Prefix;
import com.vechain.thorclient.utils.crypto.ECKeyPair;


public class ERC20ContractClient extends TransactionClient {

    /**
     * Get amount from ERC20 contract.
     * @param address address of token holder.
     * @param token {@link ERC20Token} required, the token {@link ERC20Token}
     * @param revision {@link Revision} if it is null, it will fallback to default {@link Revision#BEST}
     * @return {@link Amount}
     * @throws ClientIOException {@link ClientIOException}
     */
    public static Amount getERC20Balance(Address address, ERC20Token token, Revision revision) throws ClientIOException {
        Address contractAddr = token.getContractAddress();
        Revision currRevision = revision;
        if(currRevision == null){
            currRevision = Revision.BEST;
        }
        AbiDefinition abiDefinition = ERC20Contract.defaultERC20Contract.findAbiDefinition("balanceOf");
        ContractCall call = ERC20Contract.buildCall( abiDefinition, address.toHexString( null ) );
        ContractCallResult contractCallResult = callContract(call, contractAddr,  currRevision );
        if(contractCallResult == null){
            return null;
        }
        return contractCallResult.getBalance( token );
    }

    /**
     * Transfer ERC20 token
     * @param receivers {@link Address} array
     * @param amounts
     * @param token
     * @param gas
     * @param gasCoef
     * @param expiration
     * @param keyPair
     * @return
     * @throws ClientIOException
     */
    public static TransferResult transferERC20Token(Address[] receivers, Amount[] amounts, ERC20Token token, int gas, byte gasCoef, int expiration , ECKeyPair keyPair) throws ClientIOException{
        if(receivers == null){
            throw ClientArgumentException.exception( "receivers is null" );
        }
        if (amounts == null){
            throw ClientArgumentException.exception( "amounts is null" );
        }
        if(receivers.length != amounts.length){
            throw ClientArgumentException.exception( "receivers length equal to amounts length." );
        }
        if(token == null){
            throw ClientArgumentException.exception( "token is null." );
        }

        AbiDefinition abi = ProtoTypeContract.defaultContract.findAbiDefinition( "transfer" );
        if(abi == null){
            throw new IllegalArgumentException( "Can not find abi master method" );
        }
        ToClause[] clauses = new ToClause[receivers.length];
        for(int index = 0; index < receivers.length; index ++){
            clauses[index] = ProtoTypeContract.buildToClause(
                    token.getContractAddress(),
                    abi,
                    receivers[index].toHexString( Prefix.ZeroLowerX ),
                    amounts[index].toHexString());

        }
        return invokeContractMethod( clauses,gas,gasCoef,expiration, keyPair );

    }

}
