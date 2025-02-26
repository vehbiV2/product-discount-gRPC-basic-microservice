package com.vehbiozcan.productservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueItem {

    private  String filePath;
    private  int thread;
    private  int rampUp;
    private  int fileSize;

}


