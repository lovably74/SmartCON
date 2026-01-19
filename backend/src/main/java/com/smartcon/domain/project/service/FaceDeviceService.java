package com.smartcon.domain.project.service;

import com.smartcon.domain.project.dto.FaceDeviceRequest;
import com.smartcon.domain.project.dto.FaceDeviceResponse;

import java.util.List;

/**
 * 안면인식기 디바이스 관리 서비스 인터페이스
 */
public interface FaceDeviceService {

    /**
     * 안면인식기 디바이스 등록
     */
    FaceDeviceResponse registerDevice(FaceDeviceRequest request);

    /**
     * 프로젝트별 디바이스 목록 조회
     */
    List<FaceDeviceResponse> getProjectDevices(Long projectId);

    /**
     * 디바이스 상세 조회
     */
    FaceDeviceResponse getDeviceById(Long deviceId);

    /**
     * 디바이스 정보 수정
     */
    FaceDeviceResponse updateDevice(Long deviceId, FaceDeviceRequest request);

    /**
     * 디바이스 삭제
     */
    void deleteDevice(Long deviceId);

    /**
     * 활성 디바이스 목록 조회
     */
    List<FaceDeviceResponse> getActiveDevices();
}
