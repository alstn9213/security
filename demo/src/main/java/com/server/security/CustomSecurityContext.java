package com.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public class CustomSecurityContext implements SecurityContext {
  // SecurityContext 는 이런식으로 구성 돼 있다.
  // 실무에서 SecurityContext 를 커스텀하는 경우는 그다지 없다.
  // 그냥 예시로 만들어둔 클래스이므로, 이 클래스를 직접 쓰진 않을 것이다.

    private Authentication authentication;
    private String customMetadata; // 추가하고 싶은 필드

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
    
    // 추가 메서드
    public void setCustomMetadata(String meta) { 
      this.customMetadata = meta; 
    }

    public String getCustomMetadata() { 
      return this.customMetadata; 
    }

    public boolean equals(Object obj) {
      return super.equals(obj);
    }

    public int hashCode() {
      return super.hashCode();
    }

    public String toString() {
      return super.toString();
    }
}

