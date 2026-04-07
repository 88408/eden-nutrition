package eden.service;

import eden.pojo.vo.DashboardStatItemVO;
import eden.pojo.vo.SalesRevenueVO;

import java.util.List;

public interface DashboardService {
    List<DashboardStatItemVO> getDashboardStats();
    List<SalesRevenueVO> getSalesRevenue(Integer days);
}
