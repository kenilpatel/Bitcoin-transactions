import java.util.ArrayList; 
 

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	public UTXOPool pool;
	
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	pool=new UTXOPool(utxoPool);
    	return;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	if(tx==null)
    	{
    		return false;
    	}
    	ArrayList<Transaction.Input> all_in_coins=tx.getInputs();
    	ArrayList<Transaction.Output> all_out_coins=tx.getOutputs();
    	double total_in=0;
    	double total_out=0;
    	
    	//(1) all outputs claimed by {@code tx} are in the current UTXO pool
    	
    	for(Transaction.Input c:all_in_coins)
    	{
    		if(c==null)
    		{
    			return false;
    		}
    		UTXO temp_utxo=new UTXO(c.prevTxHash,c.outputIndex);
    		if(!pool.contains(temp_utxo))
    		{
    			return false;
    		}
    	}
    	
    	//(2) the signatures on each input of {@code tx} are valid
    	
    	int index=0;
    	for(Transaction.Input c:all_in_coins)
    	{
    		if(c==null)
    		{
    			return false;
    		} 
    		UTXO temp_utxo=new UTXO(c.prevTxHash,c.outputIndex);
    		Transaction.Output sender=pool.getTxOutput(temp_utxo);
    		if(sender==null)
    		{
    			return false;
    		}
    		if(!Crypto.verifySignature(sender.address,tx.getRawDataToSign(index),c.signature))
    		{
    			return false;
    		}
    		total_in=total_in+sender.value;
    		index++;
    	}
    	
    	//(3) no UTXO is claimed multiple times by {@code tx}
    	
    	UTXOPool local_pool=new UTXOPool();
    	for(Transaction.Input c:all_in_coins)
    	{
    		if(c==null)
    		{
    			return false;
    		}
    		UTXO temp_utxo=new UTXO(c.prevTxHash,c.outputIndex);
    		Transaction.Output sender=pool.getTxOutput(temp_utxo);
    		if(local_pool.contains(temp_utxo))
    		{
    			return false;
    		}
    		else
    		{
    			local_pool.addUTXO(temp_utxo,sender);
    		} 
    	}
    	
    	//(4) all of {@code tx}s output values are non-negative
    	
    	for(Transaction.Output c:all_out_coins)
    	{
    		if(c==null)
    		{
    			return false;
    		}
    		if(c.value<0)
    		{
    			return false;
    		}
    		total_out=total_out+c.value;
    	}
    	
    	//(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
    	
    	if(total_in>=total_out)
    	{
    	}
    	else
    	{
    		return false;
    	}
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	if(possibleTxs==null)
    	{
    		return new Transaction[0];
    	}
    	ArrayList<Transaction> validTxs = new ArrayList<>();
        for(int i=0;i<possibleTxs.length;i++)
    	{
    		if(isValidTx(possibleTxs[i]))
    		{
    			validTxs.add(possibleTxs[i]);
    			ArrayList<Transaction.Input> all_in_coins=possibleTxs[i].getInputs();
    	    	ArrayList<Transaction.Output> all_out_coins=possibleTxs[i].getOutputs();
    	    	for(Transaction.Input c:all_in_coins)
    	    	{
    	    		UTXO temp_utxo=new UTXO(c.prevTxHash,c.outputIndex);
    	    		pool.removeUTXO(temp_utxo);
    	    	}
    	    	int index=0;
    	    	for(Transaction.Output c:all_out_coins)
    	    	{
    	    		UTXO temp_utxo=new UTXO(possibleTxs[i].getHash(),index);
    	    		pool.addUTXO(temp_utxo, c);
    	    		index++;
    	    	}
    		}
    	}
    	return validTxs.toArray(new Transaction[validTxs.size()]);
    	
    }

}
