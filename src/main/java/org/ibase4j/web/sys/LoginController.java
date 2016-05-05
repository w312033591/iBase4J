package org.ibase4j.web.sys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.ibase4j.core.config.Resources;
import org.ibase4j.core.support.HttpCode;
import org.ibase4j.core.util.Request2ModelUtil;
import org.ibase4j.core.util.SecurityUtil;
import org.ibase4j.mybatis.generator.model.SysUser;
import org.ibase4j.service.sys.SysUserService;
import org.ibase4j.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController extends BaseController {
	@Autowired
	private SysUserService sysUserService;

	// 登录
	@ResponseBody
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelMap login(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "password", required = false) String password) {
		Assert.notNull(account, Resources.getMessage("ACCOUNT_IS_NULL"));
		Assert.notNull(password, Resources.getMessage("PASSWORD_IS_NULL"));
		UsernamePasswordToken token = new UsernamePasswordToken(account, SecurityUtil.encryptSHA(password));
		token.setRememberMe(true);
		Subject subject = SecurityUtils.getSubject();
		try {
			subject.login(token);
			if (subject.isAuthenticated()) {
				return setSuccessModelMap(modelMap);
			}
		} catch (LockedAccountException e) {
			throw new IllegalArgumentException(Resources.getMessage("ACCOUNT_LOCKED", token.getPrincipal()));
		} catch (DisabledAccountException e) {
			throw new IllegalArgumentException(Resources.getMessage("ACCOUNT_DISABLED", token.getPrincipal()));
		} catch (ExpiredCredentialsException e) {
			throw new IllegalArgumentException(Resources.getMessage("ACCOUNT_EXPIRED", token.getPrincipal()));
		} catch (Exception e) {
			throw new IllegalArgumentException(Resources.getMessage("LOGIN_FAIL"), e);
		}
		throw new IllegalArgumentException(Resources.getMessage("LOGIN_FAIL"));
	}

	// 登出
	@ResponseBody
	@RequestMapping("/logout")
	public ModelMap logout(ModelMap modelMap, HttpServletRequest request) {
		SecurityUtils.getSubject().logout();
		return setSuccessModelMap(modelMap);
	}

	// 注册
	@ResponseBody
	@RequestMapping(value = "/regin", method = RequestMethod.POST)
	public ModelMap regin(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "password", required = false) String password) {
		Assert.notNull(account, Resources.getMessage("ACCOUNT_IS_NULL"));
		Assert.notNull(password, Resources.getMessage("PASSWORD_IS_NULL"));
		SysUser sysUser = Request2ModelUtil.covert(SysUser.class, request);
		sysUser.setPassword(SecurityUtil.encryptSHA(password));
		sysUserService.update(sysUser);
		return setSuccessModelMap(modelMap);
	}

	// 没有权限
	@ResponseBody
	@RequestMapping("/unauthorized")
	public ModelMap unauthorized(ModelMap modelMap) {
		return setModelMap(modelMap, HttpCode.FORBIDDEN);
	}
}
