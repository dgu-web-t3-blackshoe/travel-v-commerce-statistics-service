package com.travelvcommerce.statisticsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelvcommerce.statisticsservice.dto.*;
import com.travelvcommerce.statisticsservice.dto.count.AdClickCountDto;
import com.travelvcommerce.statisticsservice.dto.count.LikeCountDto;
import com.travelvcommerce.statisticsservice.dto.count.VideoCountInfoDto;
import com.travelvcommerce.statisticsservice.dto.count.ViewCountDto;
import com.travelvcommerce.statisticsservice.exception.UserAlreadyClickedAdException;
import com.travelvcommerce.statisticsservice.exception.UserDidNotLikedVideoException;
import com.travelvcommerce.statisticsservice.service.KafkaVideoInfoProducerService;
import com.travelvcommerce.statisticsservice.exception.UserAlreadyLikedVideoException;
import com.travelvcommerce.statisticsservice.exception.UserAlreadyViewedVideoException;
import com.travelvcommerce.statisticsservice.service.StatisticsUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics-service")
public class StatisticsUpdateController {
    private final StatisticsUpdateService statisticsUpdateService;
    private final KafkaVideoInfoProducerService kafkaVideoInfoProducerService;
    private final ObjectMapper objectMapper;

    @PutMapping("/{videoId}/views")
    public ResponseEntity<ResponseDto> increaseViewCount(@PathVariable("videoId") String videoId,
                                                         @RequestBody ViewCountDto.ViewCountRequestDto viewCountRequestDto) {
        String userId = viewCountRequestDto.getUserId();
        VideoCountInfoDto videoCountInfoDto;

        try {
            videoCountInfoDto = statisticsUpdateService.increaseViewCount(videoId, userId);
        } catch (UserAlreadyViewedVideoException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (NoSuchElementException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        } catch (RuntimeException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }

        kafkaVideoInfoProducerService.updateVideoStatistics(videoCountInfoDto);

        ViewCountDto.ViewCountResponseDto viewCountResponseDto = ViewCountDto.ViewCountResponseDto.builder()
                .videoId(videoId)
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()).toString())
                .build();

        ResponseDto responseDto = ResponseDto.buildResponseDto(objectMapper.convertValue(viewCountResponseDto, Map.class));
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PutMapping("/{videoId}/likes")
    public ResponseEntity<ResponseDto> changeLikeCount(@PathVariable("videoId") String videoId,
                                                         @RequestBody LikeCountDto.LikeRequestDto likeRequestDto) {
        String userId = likeRequestDto.getUserId();
        String action = likeRequestDto.getAction();
        VideoCountInfoDto videoCountInfoDto;
        try {
            if (action.equals("like")) {
                videoCountInfoDto = statisticsUpdateService.increaseVideoLikeCount(videoId, userId);
            } else if (action.equals("dislike")) {
                videoCountInfoDto = statisticsUpdateService.decreaseVideoLikeCount(videoId, userId);
            } else {
                throw new IllegalArgumentException("action must be like or dislike");
            }
        } catch (UserAlreadyLikedVideoException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (UserDidNotLikedVideoException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (NoSuchElementException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        } catch (RuntimeException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }

        kafkaVideoInfoProducerService.updateVideoStatistics(videoCountInfoDto);

        LikeCountDto.LikeResponseDto likeResponseDto = LikeCountDto.LikeResponseDto.builder()
                .videoId(videoId)
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()).toString())
                .build();
        ResponseDto responseDto = ResponseDto.buildResponseDto(objectMapper.convertValue(likeResponseDto, Map.class));
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @PutMapping("/{adId}/adClicks")
    public ResponseEntity<ResponseDto> increaseAdClickCount(@PathVariable("adId") String adId,
                                                            @RequestBody AdClickCountDto.AdClickRequestDto adClickRequestDto) {
        String userId = adClickRequestDto.getUserId();
        VideoCountInfoDto videoCountInfoDto;

        try {
            videoCountInfoDto = statisticsUpdateService.increaseVideoAdClickCount(adId, userId);
        } catch (UserAlreadyClickedAdException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (NoSuchElementException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        } catch (RuntimeException e) {
            ResponseDto responseDto = ResponseDto.buildResponseDto(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
        }

        kafkaVideoInfoProducerService.updateVideoStatistics(videoCountInfoDto);

        AdClickCountDto.AdClickResponseDto adClickResponseDto = AdClickCountDto.AdClickResponseDto.builder()
                .adId(adId)
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()).toString())
                .build();
        ResponseDto responseDto = ResponseDto.buildResponseDto(objectMapper.convertValue(adClickResponseDto, Map.class));
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
