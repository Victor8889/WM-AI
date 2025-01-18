package com.hncboy.beehive.base.enums;

import java.util.List;

/**
 * @author ll
 * @date 2023-9-10
 */
public class RecordsEnumManager {
    public static List<RecordsEnum> recordsEnums;

    static {
        // 从数据库获取枚举数据，并赋值给recordsEnums
        // 这里可以使用你自己的数据库操作方法
        //recordsEnums = yourDatabaseService.getRecordsEnums();
    }

    public static List<RecordsEnum> getRecordsEnums() {
        return recordsEnums;
    }

    public static RecordsEnum getById(Integer id) {
        for (RecordsEnum recordsEnum : recordsEnums) {
            if (recordsEnum.getId().equals(id)) {
                return recordsEnum;
            }
        }
        return null;
    }
}
