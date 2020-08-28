package com.cloudpos.jniinterface;

public class TerminalStatusInterface {
    /**
     * Get usage count.
     * @param type: 0。MSR Card;1.IC Card;2.Contactless card;3.PSAMCard 1;4.PSAM Card 2;5.PSAM Card 3;6.PSAM Card 4.
     */
    public native static int getUsageCount(int type) throws NoSuchMethodException;
    
    /**
     * Get fail count.
     * @param type: 0。MSR Card;1.IC Card;2.Contactless card;3.PSAMCard 1;4.PSAM Card 2;5.PSAM Card 3;6.PSAM Card 4.
     */
    public native static int getFailCount(int type) throws NoSuchMethodException;

}
