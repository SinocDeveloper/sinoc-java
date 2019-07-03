package org.sinoc.shell.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sinoc.shell.config.HarmonyProperties;
import org.sinoc.shell.model.web.ClientLoginInfo;
import org.sinoc.shell.model.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	HarmonyProperties harmonyProperties;
	
	@RequestMapping("/login")
	public ClientLoginInfo login(String userName,String password,HttpServletRequest request){
		if(StringUtils.isBlank(password) || StringUtils.isBlank(userName)) {
			return new ClientLoginInfo(false, "error request!");
		}

		String account = harmonyProperties.getConfig().getString("modules.web.login.username");
		String pwd = harmonyProperties.getConfig().getString("modules.web.login.password");
		
		if(!StringUtils.equals(userName, account) || !StringUtils.equals(password, pwd)) {
			return new ClientLoginInfo(false, "Account or password is error!");
		}
		ClientLoginInfo  clientLoginInfo = new ClientLoginInfo(userName, password,true,"");
		request.getSession().setAttribute(WebConstants.CLIENT_INFO, clientLoginInfo);
		return clientLoginInfo;
	}
	
	@RequestMapping("/getLoginInfo")
	public ClientLoginInfo getLoginInfo(HttpServletRequest request) {
		if(request.getSession().getAttribute(WebConstants.CLIENT_INFO) != null) {
			return (ClientLoginInfo)request.getSession().getAttribute(WebConstants.CLIENT_INFO);
		}else {
			return null;
		}
	}
}
