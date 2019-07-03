package org.sinoc.shell.web.controller;

import org.sinoc.shell.config.WebEnabledCondition;
import org.sinoc.shell.model.dto.WalletInfoDTO;
import org.sinoc.shell.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Conditional(WebEnabledCondition.class)
public class WalletController {

    @Autowired
    WalletService walletService;

    @MessageMapping("/getWalletInfo")
    public WalletInfoDTO getWalletInfo() {
        return walletService.getWalletInfo();
    }

    @MessageMapping("/newAddress")
    public String newAddress(NewAddressDTO data) {
        return walletService.newAddress(data.getName(), data.getSecret());
    }

    @MessageMapping("/importAddress")
    public String importAddress(ImportAddressDTO data) {
        return walletService.importAddress(data.getAddress(), data.getName());
    }

    @MessageMapping("/removeAddress")
    public void removeAddress(StringValueDTO data) {
        walletService.removeAddress(data.value);
    }

//    @MessageMapping("/generateWords")
    @RequestMapping(value = "/wallet/generateWords", method = RequestMethod.GET)
    public List<String> generateWords(@RequestParam Integer wordsCount) {
        return walletService.generateWords(wordsCount);
    }

    public static class StringValueDTO {

         public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
         
         
    }

    public static class ImportAddressDTO {

        private String address;

        private String name;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
        
        
    }

    public static class NewAddressDTO {

        private String secret;

        private String name;

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
        
        
    }
}
