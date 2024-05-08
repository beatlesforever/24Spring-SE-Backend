package com.example.sebackend.config;

import com.example.sebackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private final IUserService userService; // 使用接口而非具体实现

    public JwtRequestFilter(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 实现JWT认证和授权流程的过滤器方法。
     * 首先从HTTP请求头中提取JWT，然后验证JWT的有效性，
     * 如果验证成功，则更新SecurityContextHolder中的认证信息。
     *
     * @param request  HttpServletRequest对象，用于获取客户端请求中的Authorization信息。
     * @param response HttpServletResponse对象，本方法中未直接使用，可用于向客户端发送响应。
     * @param chain    FilterChain对象，用于继续请求处理或访问下一个过滤器。
     * @throws ServletException 如果处理请求时发生Servlet相关异常。
     * @throws IOException      如果处理请求时发生IO相关异常。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 提取请求头中的Authorization信息
        final String authorizationHeader = request.getHeader("Authorization");

        // 解析JWT并验证
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // 从Authorization头中提取JWT
            username = JwtUtil.extractUsername(jwt); // 从JWT中解析出用户名
        }

        // 当username非空且SecurityContextHolder中没有认证信息时，执行认证流程
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 加载用户信息
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            // 创建新的认证token，包含权限信息
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // 设置认证细节
            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 更新SecurityContextHolder中的认证信息
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        // 继续执行过滤器链
        chain.doFilter(request, response);
    }


}
