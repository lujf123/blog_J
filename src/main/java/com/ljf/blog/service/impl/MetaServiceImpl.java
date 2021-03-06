package com.ljf.blog.service.impl;

import com.ljf.blog.dto.Types;
import com.ljf.blog.exception.TipException;
import com.ljf.blog.mapper.MetaMapper;
import com.ljf.blog.pojo.Meta;
import com.ljf.blog.pojo.MetaExample;
import com.ljf.blog.service.ContentService;
import com.ljf.blog.service.MetaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lujiafeng on 2018/9/18.
 */

@Service
public class MetaServiceImpl implements MetaService {

    @Autowired
    MetaMapper metaMapper;
    @Autowired
    ContentService contentService;

    @Override
    public Meta getMeta(String type, String name) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
            MetaExample metaExample = new MetaExample();
            metaExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
            List<Meta> metas = metaMapper.selectByExample(metaExample);
            if (!metas.isEmpty()) {
                return metas.get(0);
            }
        }
        return null;
    }


    @Override
    public List<Meta> getMetas(String type) {
        if (StringUtils.isNotBlank(type)) {
            MetaExample metaExample = new MetaExample();
            metaExample.setOrderByClause("sort desc, mid desc");
            metaExample.createCriteria().andTypeEqualTo(type);
            List<Meta> metas = metaMapper.selectByExample(metaExample);
            return metas;
        }
        return null;
    }

    @Override
    public void saveMeta(Integer cid, String names, String type) {
        if (null == cid) {
            throw new TipException("项目关联id不能为空");
        }
        if (StringUtils.isNotBlank(names) && StringUtils.isNotBlank(type)) {
            String[] nameArr = StringUtils.split(names, ",");
            for (String name : nameArr) {
                this.saveOrUpdate(cid, name, type);
            }
        }
    }

    private void saveOrUpdate(Integer cid, String name, String type) {
        MetaExample metaVoExample = new MetaExample();
        metaVoExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
        List<Meta> metaVos = metaMapper.selectByExample(metaVoExample);

        int mid;
        Meta metas;
        if (metaVos.size() == 1) {
            metas = metaVos.get(0);
            mid = metas.getMid();
        } else if (metaVos.size() > 1) {
            throw new TipException("查询到多条数据");
        } else {
            metas = new Meta();
            metas.setSlug(name);
            metas.setName(name);
            metas.setType(type);
            metaMapper.insertSelective(metas);
            mid = metas.getMid();
        }
//        if (mid != 0) {
//            Long count = relationshipService.countById(cid, mid);
//            if (count == 0) {
//                RelationshipVoKey relationships = new RelationshipVoKey();
//                relationships.setCid(cid);
//                relationships.setMid(mid);
//                relationshipService.insertVo(relationships);
//            }
//        }
    }

    @Override
    public void saveMeta(String type, String name, Integer mid) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
            MetaExample metaExample = new MetaExample();
            metaExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
            List<Meta> metas = metaMapper.selectByExample(metaExample);
            if (metas.size() != 0) {
                throw new TipException("已存在该项");
            } else {
                Meta meta = new Meta();
                meta.setName(name);
                //null不能和int类型进行=、!=的比较，能与Integer类型进行比较
                if (null != mid) {
                    Meta orignal = metaMapper.selectByPrimaryKey(mid);
                    meta.setMid(mid);
                    meta.setType(type);
                    metaMapper.updateByPrimaryKeySelective(meta);
                    //更新原有文章的categories

                } else {
                    meta.setType(type);
                    //自动生成主键
                    metaMapper.insertSelective(meta);
                }
            }
        }

    }

    @Override
    public void delete(Integer mid) {
        Meta meta = metaMapper.selectByPrimaryKey(mid);
        if (null != meta) {
            String type = meta.getType();
            String name = meta.getName();
            metaMapper.deleteByPrimaryKey(mid);
            //删除关系Relationship上关联的meta
        }
    }

    @Override
    public void update(Meta meta) {
        if (null != meta && null != meta.getMid()) {
            //根据主键更新非null字段
            metaMapper.insertSelective(meta);
        }
    }

    @Override
    public void updateCount(String category, int increment) {
        MetaExample example = new MetaExample();
        example.createCriteria().andTypeEqualTo(Types.CATEGORY.getType()).andNameEqualTo(category);
        List<Meta> metas = metaMapper.selectByExample(example);
        if (metas.size() == 1) {
            Meta tmp = metas.get(0);
            tmp.setCount(metas.get(0).getCount() + increment);
            metaMapper.updateByPrimaryKeySelective(tmp);
        } else {
            throw new TipException("更新Meta count错误");
        }
    }
}
