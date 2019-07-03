package org.sinoc.shell.service.wallet;

public class WalletAddressItem {

    public String address;

    public String name;
    
    public WalletAddressItem(){};
    
    public WalletAddressItem(String address, String name) {
        this.address = address;
        this.name = name;
    }
}
