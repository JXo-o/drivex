package com.jxh.drivex.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.coupon.mapper.CouponInfoMapper;
import com.jxh.drivex.coupon.mapper.CustomerCouponMapper;
import com.jxh.drivex.coupon.service.CouponInfoService;
import com.jxh.drivex.model.entity.coupon.CouponInfo;
import com.jxh.drivex.model.entity.coupon.CustomerCoupon;
import com.jxh.drivex.model.form.coupon.UseCouponForm;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.coupon.AvailableCouponVo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo>
        implements CouponInfoService {

    private final CouponInfoMapper couponInfoMapper;
    private final CustomerCouponMapper customerCouponMapper;
    private final RedissonClient redissonClient;

    public CouponInfoServiceImpl(
            CouponInfoMapper couponInfoMapper,
            CustomerCouponMapper customerCouponMapper,
            RedissonClient redissonClient
    ) {
        this.couponInfoMapper = couponInfoMapper;
        this.customerCouponMapper = customerCouponMapper;
        this.redissonClient = redissonClient;
    }

    /**
     * 查询未领取优惠券分页列表
     * @param pageParam 分页参数
     * @param customerId 用户id
     * @return 未领取优惠券分页列表
     */
    @Override
    public PageVo<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId) {
        Page<NoReceiveCouponVo> pageInfo = couponInfoMapper.findNoReceivePage(pageParam, customerId);
        return new PageVo<>(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    /**
     * 查询未使用优惠券分页列表
     * @param pageParam 分页参数
     * @param customerId 用户id
     * @return 未使用优惠券分页列表
     */
    @Override
    public PageVo<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId) {
        Page<NoUseCouponVo> pageInfo = couponInfoMapper.findNoUsePage(pageParam, customerId);
        return new PageVo<>(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    /**
     * 查询已使用优惠券分页列表
     * @param pageParam 分页参数
     * @param customerId 用户id
     * @return 已使用优惠券分页列表
     */
    @Override
    public PageVo<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, Long customerId) {
        Page<UsedCouponVo> pageInfo = couponInfoMapper.findUsedPage(pageParam, customerId);
        return new PageVo<>(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    /**
     * 处理用户领取优惠券的操作，包括校验优惠券的有效性、库存以及用户领取限制，并保存领取记录。
     *
     * @param customerId 用户ID，用于标识领取优惠券的用户。
     * @param couponId 优惠券ID，用于标识要领取的优惠券。
     * @return 如果领取成功，返回 {@code true}，否则抛出相应的异常。
     *
     * <p>该方法的主要操作流程如下：</p>
     * <ul>
     *     <li>查询优惠券信息 {@link CouponInfo}，如果优惠券不存在则抛出数据错误异常。</li>
     *     <li>判断优惠券是否过期，如果已过期则抛出优惠券过期异常。</li>
     *     <li>校验优惠券库存：
     *          <ul>
     *              <li>如果领取数量已达到发布数量的上限，则抛出库存不足异常。</li>
     *          </ul>
     *     </li>
     *     <li>校验每人限领数量：redisson 分布式锁
     *          <ul>
     *              <li>统计用户已领取的优惠券数量，如果已达到限领数量则抛出限领数量超限异常。</li>
     *          </ul>
     *     </li>
     *     <li>更新优惠券的领取数量，成功后保存用户的领取记录 {@link CustomerCoupon}。</li>
     *     <li>如果更新优惠券领取数量失败，则抛出库存不足异常。</li>
     * </ul>
     *
     * <p>此方法使用了 {@code @Transactional(rollbackFor = Exception.class)} 注解，表示在事务内执行，发生异常时回滚事务。</p>
     *
     * @throws DrivexException 当发生以下情况时抛出异常：
     * <ul>
     *     <li>优惠券不存在或数据错误 {@link ResultCodeEnum#DATA_ERROR}</li>
     *     <li>优惠券已过期 {@link ResultCodeEnum#COUPON_EXPIRED}</li>
     *     <li>优惠券库存不足 {@link ResultCodeEnum#COUPON_LESS}</li>
     *     <li>用户领取的优惠券数量超限 {@link ResultCodeEnum#COUPON_USER_LIMIT}</li>
     * </ul>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean receive(Long customerId, Long couponId) {
        CouponInfo couponInfo = this.getById(couponId);
        if(couponInfo == null) {
            throw new DrivexException(ResultCodeEnum.DATA_ERROR);
        }
        if (couponInfo.getExpireTime().before(new Date())) {
            throw new DrivexException(ResultCodeEnum.COUPON_EXPIRED);
        }
        if (couponInfo.getPublishCount() !=0 && couponInfo.getReceiveCount() >= couponInfo.getPublishCount()) {
            throw new DrivexException(ResultCodeEnum.COUPON_LESS);
        }
        RLock lock = redissonClient.getLock(RedisConstant.COUPON_LOCK + customerId);
        try {
            boolean flag = lock.tryLock(
                    RedisConstant.COUPON_LOCK_WAIT_TIME,
                    RedisConstant.COUPON_LOCK_LEASE_TIME,
                    TimeUnit.SECONDS
            );
            if (flag) {
                if (couponInfo.getPerLimit() > 0) {
                    long count = customerCouponMapper.selectCount(
                            new LambdaQueryWrapper<CustomerCoupon>()
                                    .eq(CustomerCoupon::getCouponId, couponId)
                                    .eq(CustomerCoupon::getCustomerId, customerId)
                    );
                    if (count >= couponInfo.getPerLimit()) {
                        throw new DrivexException(ResultCodeEnum.COUPON_USER_LIMIT);
                    }
                }
                int row = couponInfo.getPublishCount() == 0 ?
                        couponInfoMapper.updateReceiveCount(couponId) :
                        couponInfoMapper.updateReceiveCountByLimit(couponId);
                if (row == 1) {
                    this.saveCustomerCoupon(customerId, couponId, couponInfo.getExpireTime());
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("领取优惠券失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        throw new DrivexException(ResultCodeEnum.COUPON_LESS);
    }

    /**
     * 查找用户可用的优惠券列表，按照优惠金额排序。
     * <p>
     * 该方法根据用户ID和订单金额，筛选出符合条件的优惠券，并计算每张优惠券的实际减免金额。
     * 优惠券分为两种类型：
     * 1. 现金券：需要判断订单金额是否满足条件，并且订单金额减去优惠券面额后仍然大于0。
     * 2. 折扣券：计算订单折扣后的金额，判断是否满足门槛条件，且折扣后的订单金额大于门槛金额。
     * </p>
     *
     * @param customerId 用户ID，用于获取用户未使用的优惠券列表。
     * @param orderAmount 订单金额，用于计算优惠券的实际减免金额。
     * @return 返回符合条件的优惠券列表，按优惠金额升序排序。
     */
    @Override
    public List<AvailableCouponVo> findAvailableCoupon(Long customerId, BigDecimal orderAmount) {
        List<AvailableCouponVo> availableCouponVoList = new ArrayList<>();
        List<NoUseCouponVo> list = couponInfoMapper.findNoUseList(customerId);
        List<NoUseCouponVo> type1List = list.stream()
                .filter(item -> item.getCouponType() == 1)
                .toList();
        for (NoUseCouponVo noUseCouponVo : type1List) {
            BigDecimal reduceAmount = noUseCouponVo.getAmount();
            if (noUseCouponVo.getConditionAmount().doubleValue() == 0 &&
                    orderAmount.subtract(reduceAmount).doubleValue() > 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
            if (noUseCouponVo.getConditionAmount().doubleValue() > 0 &&
                    orderAmount.subtract(noUseCouponVo.getConditionAmount()).doubleValue() > 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
        }

        List<NoUseCouponVo> type2List = list.stream()
                .filter(item -> item.getCouponType() == 2)
                .toList();
        for (NoUseCouponVo noUseCouponVo : type2List) {
            BigDecimal discountOrderAmount = orderAmount
                    .multiply(noUseCouponVo.getDiscount())
                    .divideToIntegralValue(new BigDecimal("10"))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal reduceAmount = orderAmount.subtract(discountOrderAmount);
            if (noUseCouponVo.getConditionAmount().doubleValue() == 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
            if (noUseCouponVo.getConditionAmount().doubleValue() > 0 &&
                    discountOrderAmount.subtract(noUseCouponVo.getConditionAmount()).doubleValue() > 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
        }

        if (!availableCouponVoList.isEmpty()) {
            availableCouponVoList.sort(Comparator.comparing(AvailableCouponVo::getReduceAmount));
        }
        return availableCouponVoList;
    }

    /**
     * 使用优惠券
     * <p>
     * 该方法根据用户提交的优惠券信息，计算优惠券的减免金额，并更新优惠券的使用数量。
     * </p>
     *
     * @param useCouponForm 使用优惠券表单
     * @return 返回减免金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(UseCouponForm useCouponForm) {
        CustomerCoupon customerCoupon = customerCouponMapper.selectById(useCouponForm.getCustomerCouponId());
        if(customerCoupon == null) {
            throw new DrivexException(ResultCodeEnum.DATA_ERROR);
        }
        CouponInfo couponInfo = couponInfoMapper.selectById(customerCoupon.getCouponId());
        if(couponInfo == null) {
            throw new DrivexException(ResultCodeEnum.DATA_ERROR);
        }
        if(customerCoupon.getCustomerId().longValue() != useCouponForm.getCustomerId().longValue()) {
            throw new DrivexException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        BigDecimal reduceAmount = getReduceAmount(useCouponForm, couponInfo);
        if(reduceAmount.doubleValue() > 0) {
            if(couponInfoMapper.updateUseCount(couponInfo.getId()) == 1) {
                CustomerCoupon updateCustomerCoupon = new CustomerCoupon();
                updateCustomerCoupon.setId(customerCoupon.getId());
                updateCustomerCoupon.setUsedTime(new Date());
                updateCustomerCoupon.setOrderId(useCouponForm.getOrderId());
                customerCouponMapper.updateById(updateCustomerCoupon);
                return reduceAmount;
            }
        }
        throw new DrivexException(ResultCodeEnum.DATA_ERROR);
    }

    /**
     * 保存用户领取优惠券的记录。
     *
     * @param customerId 用户ID。
     * @param couponId 优惠券ID。
     * @param expireTime 优惠券的过期时间。
     */
    private void saveCustomerCoupon(Long customerId, Long couponId, Date expireTime) {
        CustomerCoupon customerCoupon = new CustomerCoupon();
        customerCoupon.setCustomerId(customerId);
        customerCoupon.setCouponId(couponId);
        customerCoupon.setStatus(1);
        customerCoupon.setReceiveTime(new Date());
        customerCoupon.setExpireTime(expireTime);
        customerCouponMapper.insert(customerCoupon);
    }

    /**
     * 构建用户未使用的最佳优惠券信息。
     *
     * @param noUseCouponVo 未使用优惠券信息。
     * @param reduceAmount 优惠金额。
     * @return 返回用户未使用的最佳优惠券信息。
     */
    private AvailableCouponVo buildBestNoUseCouponVo(NoUseCouponVo noUseCouponVo, BigDecimal reduceAmount) {
        AvailableCouponVo bestNoUseCouponVo = new AvailableCouponVo();
        BeanUtils.copyProperties(noUseCouponVo, bestNoUseCouponVo);
        bestNoUseCouponVo.setCouponId(noUseCouponVo.getId());
        bestNoUseCouponVo.setReduceAmount(reduceAmount);
        return bestNoUseCouponVo;
    }

    /**
     * 使用优惠券
     *
     * @param useCouponForm 使用优惠券表单
     * @return 返回减免金额
     */
    private BigDecimal getReduceAmount(UseCouponForm useCouponForm, CouponInfo couponInfo) {
        if(couponInfo.getCouponType() == 1) {
            if (couponInfo.getConditionAmount().doubleValue() == 0 &&
                    useCouponForm.getOrderAmount().subtract(couponInfo.getAmount()).doubleValue() > 0) {
                return couponInfo.getAmount();
            }
            if (couponInfo.getConditionAmount().doubleValue() > 0 &&
                    useCouponForm.getOrderAmount().subtract(couponInfo.getConditionAmount()).doubleValue() > 0) {
                return couponInfo.getAmount();
            }
        } else {
            BigDecimal discountOrderAmount = useCouponForm
                    .getOrderAmount()
                    .multiply(couponInfo.getDiscount())
                    .divideToIntegralValue(new BigDecimal("10"))
                    .setScale(2, RoundingMode.HALF_UP);
            if (couponInfo.getConditionAmount().doubleValue() == 0) {
                return useCouponForm.getOrderAmount().subtract(discountOrderAmount);
            }
            if (couponInfo.getConditionAmount().doubleValue() > 0 &&
                    discountOrderAmount.subtract(couponInfo.getConditionAmount()).doubleValue() > 0) {
                return useCouponForm.getOrderAmount().subtract(discountOrderAmount);
            }
        }
        return BigDecimal.ZERO;
    }
}
