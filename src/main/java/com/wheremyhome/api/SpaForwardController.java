package com.wheremyhome.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA 폴백 컨트롤러.
 *
 * React Router(BrowserRouter) 경로를 브라우저가 직접 요청/새로고침하면
 * Spring이 해당 경로를 못 찾아 404를 낸다. 이 경로들을 index.html로 forward하면
 * React가 로드되고 React Router가 주소를 보고 알맞은 페이지를 렌더한다.
 *
 * - "/"       → static/index.html 기본 서빙 (여기서 처리 안 함)
 * - "/api/**" → 기존 REST 컨트롤러가 처리 (여기서 처리 안 함)
 * - 아래 경로 → 프론트 화면 경로이므로 index.html로 forward
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/search", "/apartment/**", "/regions", "/calculator", "/compare"})
    public String forward() {
        return "forward:/index.html";
    }
}
