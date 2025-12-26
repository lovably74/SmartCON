import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Progress } from '@/components/ui/progress';
import { 
  CreditCard, 
  Calendar, 
  Users, 
  Building, 
  HardDrive, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  Clock
} from 'lucide-react';

/**
 * 본사 관리자 구독 관리 페이지
 */
const SubscriptionHQ: React.FC = () => {
  const [activeTab, setActiveTab] = useState('overview');

  // 임시 데이터 - 실제로는 API에서 가져옴
  const currentSubscription = {
    id: 1,
    plan: {
      id: 'standard',
      name: 'Standard',
      monthlyPrice: 100000,
      maxSites: 10,
      maxUsers: 200,
      maxStorageGb: 50
    },
    status: 'ACTIVE',
    startDate: '2024-01-01',
    nextBillingDate: '2024-02-01',
    billingCycle: 'MONTHLY',
    isTrial: false,
    autoRenewal: true
  };

  const usage = {
    currentSites: 5,
    currentUsers: 85,
    currentStorageGb: 12,
    usagePercentage: 42
  };

  const plans = [
    {
      id: 'basic',
      name: 'Basic',
      description: '소규모 현장을 위한 기본 요금제',
      monthlyPrice: 50000,
      yearlyPrice: 540000,
      maxSites: 3,
      maxUsers: 50,
      maxStorageGb: 10,
      features: ['안면인식 출입관리', '기본 대시보드', '모바일 앱', '이메일 지원']
    },
    {
      id: 'standard',
      name: 'Standard',
      description: '중규모 현장을 위한 표준 요금제',
      monthlyPrice: 100000,
      yearlyPrice: 1080000,
      maxSites: 10,
      maxUsers: 200,
      maxStorageGb: 50,
      features: ['안면인식 출입관리', '고급 대시보드', '모바일 앱', '전자계약', '공사일보', '이메일/전화 지원']
    },
    {
      id: 'premium',
      name: 'Premium',
      description: '대규모 현장을 위한 프리미엄 요금제',
      monthlyPrice: 200000,
      yearlyPrice: 2160000,
      maxSites: 30,
      maxUsers: 500,
      maxStorageGb: 200,
      features: ['안면인식 출입관리', '고급 대시보드', '모바일 앱', '전자계약', '공사일보', '급여 정산', 'API 연동', '우선 지원']
    }
  ];

  const paymentHistory = [
    {
      id: 1,
      amount: 100000,
      status: 'SUCCESS',
      paidAt: '2024-01-01T00:00:00',
      billingPeriodStart: '2024-01-01',
      billingPeriodEnd: '2024-01-31',
      invoiceNumber: 'INV_20240101_ABC123'
    },
    {
      id: 2,
      amount: 100000,
      status: 'SUCCESS',
      paidAt: '2023-12-01T00:00:00',
      billingPeriodStart: '2023-12-01',
      billingPeriodEnd: '2023-12-31',
      invoiceNumber: 'INV_20231201_DEF456'
    }
  ];

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge className="bg-green-100 text-green-800"><CheckCircle className="w-3 h-3 mr-1" />활성</Badge>;
      case 'SUSPENDED':
        return <Badge className="bg-yellow-100 text-yellow-800"><Clock className="w-3 h-3 mr-1" />일시중지</Badge>;
      case 'CANCELLED':
        return <Badge className="bg-red-100 text-red-800"><XCircle className="w-3 h-3 mr-1" />해지</Badge>;
      default:
        return <Badge variant="secondary">{status}</Badge>;
    }
  };

  const getPaymentStatusBadge = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <Badge className="bg-green-100 text-green-800">결제완료</Badge>;
      case 'FAILED':
        return <Badge className="bg-red-100 text-red-800">결제실패</Badge>;
      case 'PENDING':
        return <Badge className="bg-yellow-100 text-yellow-800">결제대기</Badge>;
      default:
        return <Badge variant="secondary">{status}</Badge>;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">구독 관리</h1>
          <p className="text-muted-foreground">구독 현황을 확인하고 관리하세요</p>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="overview">구독 현황</TabsTrigger>
          <TabsTrigger value="usage">사용량 모니터링</TabsTrigger>
          <TabsTrigger value="plans">요금제 변경</TabsTrigger>
          <TabsTrigger value="billing">결제 관리</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">현재 요금제</CardTitle>
                <CreditCard className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{currentSubscription.plan.name}</div>
                <p className="text-xs text-muted-foreground">
                  {formatCurrency(currentSubscription.plan.monthlyPrice)}/월
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">구독 상태</CardTitle>
                <CheckCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {getStatusBadge(currentSubscription.status)}
                </div>
                <p className="text-xs text-muted-foreground">
                  {currentSubscription.isTrial ? '체험판' : '정식 구독'}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">다음 결제일</CardTitle>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">
                  {formatDate(currentSubscription.nextBillingDate)}
                </div>
                <p className="text-xs text-muted-foreground">
                  자동 갱신 {currentSubscription.autoRenewal ? '활성' : '비활성'}
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">사용률</CardTitle>
                <AlertTriangle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{usage.usagePercentage}%</div>
                <Progress value={usage.usagePercentage} className="mt-2" />
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>구독 정보</CardTitle>
              <CardDescription>현재 구독 중인 요금제의 상세 정보입니다</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="flex items-center space-x-2">
                  <Building className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">최대 현장 수: {currentSubscription.plan.maxSites}개</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Users className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">최대 사용자 수: {currentSubscription.plan.maxUsers}명</span>
                </div>
                <div className="flex items-center space-x-2">
                  <HardDrive className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">저장 용량: {currentSubscription.plan.maxStorageGb}GB</span>
                </div>
              </div>
              <div className="flex space-x-2">
                <Button variant="outline">요금제 변경</Button>
                <Button variant="outline">구독 해지</Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="usage" className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Building className="h-5 w-5" />
                  <span>현장 사용량</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>현재 사용</span>
                    <span>{usage.currentSites}개</span>
                  </div>
                  <div className="flex justify-between">
                    <span>최대 허용</span>
                    <span>{currentSubscription.plan.maxSites}개</span>
                  </div>
                  <Progress 
                    value={(usage.currentSites / currentSubscription.plan.maxSites) * 100} 
                    className="mt-2" 
                  />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Users className="h-5 w-5" />
                  <span>사용자 수</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>현재 사용</span>
                    <span>{usage.currentUsers}명</span>
                  </div>
                  <div className="flex justify-between">
                    <span>최대 허용</span>
                    <span>{currentSubscription.plan.maxUsers}명</span>
                  </div>
                  <Progress 
                    value={(usage.currentUsers / currentSubscription.plan.maxUsers) * 100} 
                    className="mt-2" 
                  />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <HardDrive className="h-5 w-5" />
                  <span>저장 용량</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span>현재 사용</span>
                    <span>{usage.currentStorageGb}GB</span>
                  </div>
                  <div className="flex justify-between">
                    <span>최대 허용</span>
                    <span>{currentSubscription.plan.maxStorageGb}GB</span>
                  </div>
                  <Progress 
                    value={(usage.currentStorageGb / currentSubscription.plan.maxStorageGb) * 100} 
                    className="mt-2" 
                  />
                </div>
              </CardContent>
            </Card>
          </div>

          {usage.usagePercentage >= 80 && (
            <Card className="border-yellow-200 bg-yellow-50">
              <CardHeader>
                <CardTitle className="flex items-center space-x-2 text-yellow-800">
                  <AlertTriangle className="h-5 w-5" />
                  <span>사용량 주의</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-yellow-700">
                  현재 사용량이 제한의 80%를 초과했습니다. 
                  서비스 중단을 방지하기 위해 요금제 업그레이드를 고려해보세요.
                </p>
                <Button className="mt-4" variant="outline">
                  요금제 업그레이드
                </Button>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="plans" className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {plans.map((plan) => (
              <Card 
                key={plan.id} 
                className={plan.id === currentSubscription.plan.id ? 'border-blue-500 bg-blue-50' : ''}
              >
                <CardHeader>
                  <div className="flex justify-between items-start">
                    <div>
                      <CardTitle>{plan.name}</CardTitle>
                      <CardDescription>{plan.description}</CardDescription>
                    </div>
                    {plan.id === currentSubscription.plan.id && (
                      <Badge className="bg-blue-100 text-blue-800">현재 요금제</Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <div className="text-3xl font-bold">{formatCurrency(plan.monthlyPrice)}</div>
                    <div className="text-sm text-muted-foreground">월 {formatCurrency(plan.yearlyPrice)} (연간)</div>
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span>최대 현장</span>
                      <span>{plan.maxSites}개</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>최대 사용자</span>
                      <span>{plan.maxUsers}명</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span>저장 용량</span>
                      <span>{plan.maxStorageGb}GB</span>
                    </div>
                  </div>

                  <div className="space-y-1">
                    {plan.features.map((feature, index) => (
                      <div key={index} className="flex items-center space-x-2 text-sm">
                        <CheckCircle className="h-3 w-3 text-green-500" />
                        <span>{feature}</span>
                      </div>
                    ))}
                  </div>

                  <Button 
                    className="w-full" 
                    variant={plan.id === currentSubscription.plan.id ? "secondary" : "default"}
                    disabled={plan.id === currentSubscription.plan.id}
                  >
                    {plan.id === currentSubscription.plan.id ? '현재 요금제' : '변경하기'}
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="billing" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>결제 내역</CardTitle>
              <CardDescription>최근 결제 내역을 확인하세요</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {paymentHistory.map((payment) => (
                  <div key={payment.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="space-y-1">
                      <div className="font-medium">
                        {formatDate(payment.billingPeriodStart)} ~ {formatDate(payment.billingPeriodEnd)}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        송장번호: {payment.invoiceNumber}
                      </div>
                    </div>
                    <div className="text-right space-y-1">
                      <div className="font-medium">{formatCurrency(payment.amount)}</div>
                      <div>{getPaymentStatusBadge(payment.status)}</div>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>결제 수단</CardTitle>
              <CardDescription>등록된 결제 수단을 관리하세요</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-3">
                    <CreditCard className="h-8 w-8 text-muted-foreground" />
                    <div>
                      <div className="font-medium">신한카드 ****-****-****-1234</div>
                      <div className="text-sm text-muted-foreground">홍길동 | 12/26</div>
                    </div>
                  </div>
                  <Badge className="bg-blue-100 text-blue-800">기본 결제 수단</Badge>
                </div>
                <Button variant="outline" className="w-full">
                  새 결제 수단 추가
                </Button>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default SubscriptionHQ;