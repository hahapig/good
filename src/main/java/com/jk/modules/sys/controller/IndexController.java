package com.jk.modules.sys.controller;

import com.google.common.net.InternetDomainName;
import com.jk.common.base.controller.BaseController;
import com.jk.common.util.ShiroUtils;
import com.jk.modules.sys.model.Permission;
import com.jk.modules.sys.model.User;
import com.jk.modules.sys.service.PermissionService;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.system.RuntimeInfo;
import org.omg.SendingContext.RunTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * 首页
 * Created by JK on 2017/1/22.
 */
@Controller
public class IndexController extends BaseController{

    private static final String BASE_PATH = "admin/";

    @Resource
    private PermissionService permissionService;

    /*
     * @methodName: toIndex
     * @param: []
     * @description: 定义welcome-file-list页面
     * @return: java.lang.String
     * @author: cuiP
     * @date: 2017/8/5 18:48
     * @version: V1.0.0
     */
    @GetMapping(value = "")
    public String toIndex(){
        return "redirect:/admin/index";
    }

    /**
     * 首页
     * @return
     */
    @GetMapping(value = "/admin/index")
    public String index(ModelMap modelMap){
        //从shiro的session中取user
        User user = ShiroUtils.getUserEntity();

        List<Permission> menuList = permissionService.findMenuListByUserId(user.getId());
        //通过model传到页面
        modelMap.addAttribute("menuList", menuList);
        log.info("------进入首页-------");
        return BASE_PATH+"index";
    }

    /**
     * 欢迎页
     * @return
     */
    @GetMapping(value = "/admin/welcome")
    public String welcome(ModelMap modelMap) throws UnknownHostException, SocketException {
        String hostName = InetAddress.getLocalHost().getHostName();
        Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        String ipAddress = null;
        while (allNetInterfaces.hasMoreElements())
        {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            if (netInterface.isVirtual()) {
                break;
            }
            System.out.println(netInterface.getName());
            Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements())
            {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1"))
                {
                    ipAddress = ip.toString();
                    break;
                }
            }
        }
        String domain = ipAddress;
        String port = "8081";
        RuntimeInfo runtimeInfo = new RuntimeInfo();

        String osName = System.getProperty("os.name") + "/" + System.getProperty("os.version");; //操作系统名称
        String currentTime = DateUtil.now();
        Integer coreNum = Runtime.getRuntime().availableProcessors();
        Long freeMem = runtimeInfo.getFreeMemory()/1024/1024;
        Long total = runtimeInfo.getTotalMemory()/1024/1024;

        modelMap.addAttribute("hostName", hostName);
        modelMap.addAttribute("ipAddress", ipAddress);
        modelMap.addAttribute("domain", domain);
        modelMap.addAttribute("port", port);
        modelMap.addAttribute("osName", osName);
        modelMap.addAttribute("currentTime", currentTime);
        modelMap.addAttribute("coreNum", coreNum);
        modelMap.addAttribute("freeMem", freeMem + "MB");
        modelMap.addAttribute("total", total + "MB");
        log.info("------进入欢迎页-------");
        return BASE_PATH+"welcome";
    }

    /**
     * 未授权页面
     * @return
     */
    @GetMapping(value = "/admin/403")
    public String unauthorized(){
        log.info("------没有权限-------");
        return BASE_PATH+"common/403";
    }
}
