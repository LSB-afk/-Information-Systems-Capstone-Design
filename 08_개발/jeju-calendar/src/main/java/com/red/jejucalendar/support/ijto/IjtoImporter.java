package com.red.jejucalendar.support.ijto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IjtoImporter {
    public record Result(int snapshots, int regions, int observations, int shares, int insertedRows) {}

    @Transactional
    public Result importDataset(IjtoDataset dataset) {
        return new Result(0, 0, 0, 0, 0);
    }
}
