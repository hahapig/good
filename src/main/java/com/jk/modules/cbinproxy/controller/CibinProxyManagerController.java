package com.jk.modules.cbinproxy.controller;

import com.github.pagehelper.PageInfo;
import com.jk.common.annotation.OperationLog;
import com.jk.common.base.controller.BaseController;
import com.jk.common.security.token.FormToken;
import com.jk.modules.cbinproxy.model.CibinApi;
import com.jk.modules.cbinproxy.service.CibinProxyApiManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * @author yangtao
 */
@Controller
@Slf4j
@RequestMapping("/admin/cibinproxy")
public class CibinProxyManagerController extends BaseController {

    private static final String BASE_PATH = "admin/cibinproxy/";

    @Autowired
    private CibinProxyApiManagerService cibinProxyApiManagerService;

    /**
     * 分页查询管理员列表
     *
     * @param pageNum 当前页码
     * @return
     */
    @RequiresPermissions("cibinproxy")
    @GetMapping(value = "/api")
    public String list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, ModelMap modelMap) throws Exception {
        try {
            log.debug("分页查询CIBINPROXY列表参数! pageNum = {}", pageNum);
            PageInfo<CibinApi> pageInfo = cibinProxyApiManagerService.findPage(pageNum, PAGESIZE);
            log.info("分页查询CIBINPROXY列表结果！ pageInfo = {}", pageInfo);
            modelMap.put("pageInfo", pageInfo);
        } catch (Exception e) {
            log.error("分页查询CIBINPROXY异常", e);
        }
        return BASE_PATH + "api-list";
    }

    @RequiresPermissions("cibinproxy")
    @GetMapping(value = "/api/add")
    public String add() throws Exception {
        return BASE_PATH + "api-add";
    }

    /**
     * 根据主键ID删除管理员
     *
     * @param id
     * @return
     */
    @OperationLog(value = "删除CIBINPROXY_API")
    @RequiresPermissions("cibinproxy")
    @DeleteMapping(value = "/api/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long id, @RequestParam(value = "apiName", required = false) String apiName) {
        log.info("删除cibinproxy_api! id = {}, apiName = {}", id, apiName);

        CibinApi user = cibinProxyApiManagerService.findById(id);
        if (null == user) {
            log.info("cibinproxy_api不存在! id = {}，apiName = {}", id, apiName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("cibinproxy_api不存在!");
        }

        cibinProxyApiManagerService.deleteById(id);
        log.info("cibinproxy_api删除成功! id = {}, apiName = {}", id, apiName);
        return ResponseEntity.ok("已删除!");
    }

    /**
     * 添加管理员
     *
     * @return
     */
    @OperationLog(value = "添加CINBINPROXY_API")
    @RequiresPermissions("cibinproxy")
    @ResponseBody
    @PostMapping(value = "/api/save")
    public ModelMap saveApi(CibinApi cibinApi) throws Exception {
        ModelMap messagesMap = new ModelMap();

        log.debug("添加CINBINPROXY_API! cibinApi = {}", cibinApi);
        Integer flag = cibinProxyApiManagerService.save(cibinApi);
        if (flag != null && flag >= 1) {
            log.info("添加CINBINPROXY_API 成功! cibinApiId = {}", cibinApi.getId());
            messagesMap.put("status", SUCCESS);
            messagesMap.put("message", "添加成功!");
            return messagesMap;
        }
        log.info("添加CINBINPROXY_API失败, 但没有抛出异常! userId = {}", cibinApi.getId());
        messagesMap.put("status", FAILURE);
        messagesMap.put("message", "添加失败!");
        return messagesMap;
    }

    /**
     * 跳转到cinbinProxy编辑页面
     *
     * @return
     */
    @FormToken(save = true)
    @RequiresPermissions("cibinproxy")
    @GetMapping(value = "/api/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap modelMap) {
        CibinApi cibinApi = cibinProxyApiManagerService.findById(id);
        log.info("跳转到cibinproxy编辑页面！id = {}", id);
        modelMap.put("model", cibinApi);
        return BASE_PATH + "api-edit";
    }

    /**
     * 更新管理员信息
     *
     * @return
     */
    @OperationLog(value = "编辑cibinproxy")
    @RequiresPermissions("cibinproxy")
    @ResponseBody
    @PutMapping(value = "/api/update")
    public ModelMap updateApi(CibinApi cibinApi) throws Exception {
        ModelMap messagesMap = new ModelMap();
        log.debug("编辑cibinproxy参数!  cibinApi = {}", cibinApi);

        CibinApi u = cibinProxyApiManagerService.findById(cibinApi.getId());
        if (null == u) {
            log.info("编辑cibinproxy不存在!");
            messagesMap.put("status", FAILURE);
            messagesMap.put("message", "编辑cibinproxy不存在!");
            return messagesMap;
        }


        Integer flag = cibinProxyApiManagerService.update(cibinApi);
        if (flag != null && flag >= 1) {
            log.info("编辑cibinproxy成功!cibinApi = {}", cibinApi);
            messagesMap.put("status", SUCCESS);
            messagesMap.put("message", "编辑成功!");
            return messagesMap;
        }
        log.info("编辑cibinproxy失败,但没有抛出异常 !  cibinApi = {}", cibinApi);
        messagesMap.put("status", FAILURE);
        messagesMap.put("message", "编辑失败!");
        return messagesMap;
    }

}
