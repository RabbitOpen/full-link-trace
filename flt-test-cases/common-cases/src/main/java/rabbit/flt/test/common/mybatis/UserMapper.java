package rabbit.flt.test.common.mybatis;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import rabbit.flt.test.common.bean.User;

@Repository
public interface UserMapper {

    @Select("select * from User")
    User plusGetById(@Param("id") String id);
}
