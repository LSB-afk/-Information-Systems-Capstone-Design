package com.red.jejucalendar.support.ijto;

import java.nio.file.Path;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 명시적으로 --ijto.import=경로 를 준 경우에만 실제 DB에 적재함 */
@Component
@ConditionalOnProperty(name = "ijto.import")
public class IjtoImportRunner implements ApplicationRunner {
    private final IjtoImporter importer;

    public IjtoImportRunner(IjtoImporter importer) { this.importer = importer; }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var data = IjtoDataset.load(Path.of(args.getOptionValues("ijto.import").get(0)));
        var result = importer.importDataset(data);
        System.out.printf("[IJTO] snapshots=%d regions=%d observations=%d shares=%d insertedRows=%d%n",
                result.snapshots(), result.regions(), result.observations(), result.shares(), result.insertedRows());
    }
}
