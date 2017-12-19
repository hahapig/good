package com.jk.modules.cbinproxy.model;

import lombok.Data;

@Data
public class CibinApiReponse <T> {
  private String code;
  private String msg;
  private T data;

}
