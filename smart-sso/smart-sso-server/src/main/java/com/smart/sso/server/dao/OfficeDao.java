package com.smart.sso.server.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.smart.mvc.dao.DynamicDao;
import com.smart.sso.server.model.Office;


@Mapper
public interface OfficeDao extends DynamicDao<Office> {
  public List<Office> getChildNodes(Integer nodeId);
}
