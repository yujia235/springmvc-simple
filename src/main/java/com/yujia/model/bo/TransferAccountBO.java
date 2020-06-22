package com.yujia.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferAccountBO {
    private String fromCardNo;
    private String toCardNo;
    private int money;
}
