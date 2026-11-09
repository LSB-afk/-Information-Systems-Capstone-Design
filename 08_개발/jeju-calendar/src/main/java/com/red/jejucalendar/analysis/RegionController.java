package com.red.jejucalendar.analysis;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.domain.Region;

/** 지역 선택 목록 (분석·제안·한눈에 보기 화면의 지역 선택) */
@RestController
public class RegionController {

    private final RegionDao regionDao;

    public RegionController(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    @GetMapping("/api/regions")
    public List<Region> list(@CurrentUser LoginUser user) {
        return regionDao.findAll();
    }
}
