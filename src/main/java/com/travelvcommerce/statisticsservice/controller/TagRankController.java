package com.travelvcommerce.statisticsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelvcommerce.statisticsservice.dto.RankResponseDto;
import com.travelvcommerce.statisticsservice.dto.ResponseDto;
import com.travelvcommerce.statisticsservice.service.TagRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/statistics-service")
@RestController
public class TagRankController {
    @Autowired
    private TagRankService tagRankService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/tag/region/rank")
    public ResponseEntity<ResponseDto> getTagRankByRegion() {
        RankResponseDto.TagRankResponseDto tagRankResponseDto;
        try {
            tagRankResponseDto = tagRankService.getTagRankByRegion();
        } catch (Exception e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
        ResponseDto responseDto = ResponseDto.buildResponseDto(objectMapper.convertValue(tagRankResponseDto, Map.class));

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/tag/theme/rank")
    public ResponseEntity<ResponseDto> getTagRankByTheme() {
        RankResponseDto.TagRankResponseDto tagRankResponseDto;
        try {
            tagRankResponseDto = tagRankService.getTagRankByTheme();
        } catch (Exception e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }
        ResponseDto responseDto = ResponseDto.buildResponseDto(objectMapper.convertValue(tagRankResponseDto, Map.class));

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}