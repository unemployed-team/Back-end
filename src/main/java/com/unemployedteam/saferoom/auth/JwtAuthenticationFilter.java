package com.unemployedteam.saferoom.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    try {
      jwtProvider.validate(token);
      Long userId = jwtProvider.getUserId(token);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(userId, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (CustomException e) {
      sendErrorResponse(response, e);
      return;
    }

    chain.doFilter(request, response);
  }

  private void sendErrorResponse(HttpServletResponse response, CustomException e)
      throws IOException {
    response.setStatus(e.getErrorCode().getStatus().value());
    response.setContentType("application/json;charset=UTF-8");
    ErrorResponse body = ErrorResponse.builder()
        .status(e.getErrorCode().getStatus().value())
        .message(e.getErrorCode().getMessage())
        .build();
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}