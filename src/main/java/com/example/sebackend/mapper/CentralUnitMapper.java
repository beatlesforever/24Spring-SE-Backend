package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Mapper
public interface CentralUnitMapper extends BaseMapper<CentralUnit> {
    //获取中央空调
    @Select("select * from central_unit")
    CentralUnit getCentral();

    //更新中央空调

    void update(CentralUnit centralUnit);

    @Update("update central_unit set frequency = #{frequency}")//只有一个中央空调
    void updateFrequency(int frequency);
}
