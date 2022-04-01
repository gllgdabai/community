package com.dabai.community.controller;

import com.dabai.community.annotation.LoginRequired;
import com.dabai.community.service.UserService;
import com.dabai.community.utils.CommunityUtil;
import com.dabai.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author
 * @create 2022-03-31 15:50
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;  // 上传路径

    @Value("${community.path.domain}")
    private String domain;  // 域名

    @Value("${server.servlet.context-path}")
    private String contextPath; // 项目访问路径

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImg, Model model) {
        if (headerImg == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        // 获取图片的后缀(png/jpg/jpeg)
        String filename = headerImg.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        if (!(suffix.equals(".png") || suffix.equals(".jpg") || suffix.equals(".jpeg"))) {
            model.addAttribute("error", "图片仅支持png、jpg、jpeg格式!");
            return "/site/setting";
        }
        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的位置
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImg.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // 访问路径为：http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(hostHolder.getUser().getId(),headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
     public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 获取文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);

        try (
                //放在try中，final会自动关闭
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ){
            byte[] buffer = new byte[1024];
            int data = 0;   //游标
            while ((data = fis.read(buffer)) != -1) {
                os.write(buffer, 0, data);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }
    @LoginRequired
    @PostMapping("/updatePwd")
    public String updatePwd(Model model, String oldPassword, String newPassword, String confirmPassword) {
        // map携带错误信息，为空则说明成功
        Map<String, Object> map = userService.updatePassword(oldPassword, newPassword, confirmPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/index";
        }else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }
}
