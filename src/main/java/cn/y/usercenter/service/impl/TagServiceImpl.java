package cn.y.usercenter.service.impl;

import cn.y.usercenter.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.y.usercenter.service.TagService;
import cn.y.usercenter.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author Youngman
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2025-07-12 19:49:03
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




