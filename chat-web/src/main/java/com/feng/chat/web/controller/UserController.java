package com.feng.chat.web.controller;

import com.feng.chat.web.Constants;
import com.feng.chat.web.entity.User;
import com.feng.chat.web.service.UserService;
import com.feng.chat.web.vo.MessageContactVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Description TODO
 * @Author fengsy
 * @Date 11/28/21
 */
@Controller
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(path = "/")
    public String welcomePage(@RequestParam(name = "username", required = false)
                                      String username, HttpSession session) {
        if (session.getAttribute(Constants.SESSION_KEY) != null) {
            return "index";
        } else {
            return "login";
        }
    }

    @RequestMapping(path = "/login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        try {
            User loginUser = userService.login(email, password);
            model.addAttribute("loginUser", loginUser);
            session.setAttribute(Constants.SESSION_KEY, loginUser);

            List<User> otherUsers = userService.getAllUsersExcept(loginUser);
            model.addAttribute("otherUsers", otherUsers);

            MessageContactVO contactVO = userService.getContacts(loginUser);
            model.addAttribute("contactVO", contactVO);
            return "index";

        } catch (Exception e1) {
            model.addAttribute("errormsg", email + ": 登录失败");
            return "login";
        }
    }

    @RequestMapping(path = "/ws")
    public String ws(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute(Constants.SESSION_KEY);
        model.addAttribute("loginUser", loginUser);
        List<User> otherUsers = userService.getAllUsersExcept(loginUser);
        model.addAttribute("otherUsers", otherUsers);

        MessageContactVO contactVO = userService.getContacts(loginUser);
        model.addAttribute("contactVO", contactVO);
        return "index_ws";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 移除session
        session.removeAttribute(Constants.SESSION_KEY);
        return "redirect:/";
    }
}
