import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { 
  Clock, 
  XCircle, 
  AlertTriangle, 
  Ban,
  Mail,
  Phone,
  RefreshCw,
  Plus
} from 'lucide-react';

export type SubscriptionStatus = 
  | 'PENDING_APPROVAL' 
  | 'REJECTED' 
  | 'SUSPENDED' 
  | 'TERMINATED' 
  | 'ACTIVE'
  | 'CANCELLED'
  | 'EXPIRED';

interface SubscriptionStatusDisplayProps {
  status: SubscriptionStatus;
  rejectionReason?: string;
  suspensionReason?: string;
  terminationReason?: string;
  onReapply?: () => void;
  onNewSubscription?: () => void;
  contactEmail?: string;
  contactPhone?: string;
}

/**
 * 구독 상태별 메시지 및 안내를 표시하는 컴포넌트
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
const SubscriptionStatusDisplay: React.FC<SubscriptionStatusDisplayProps> = ({
  status,
  rejectionReason,
  suspensionReason,
  terminationReason,
  onReapply,
  onNewSubscription,
  contactEmail = 'support@smartcon.co.kr',
  contactPhone = '1588-1234'
}) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'PENDING_APPROVAL':
        return {
          icon: <Clock className="h-8 w-8 text-yellow-500" />,
          title: '승인 대기 중',
          description: '구독 신청이 접수되었습니다',
          message: '관리자가 구독 신청을 검토 중입니다. 승인까지 최대 1-2 영업일이 소요될 수 있습니다.',
          variant: 'warning' as const,
          bgColor: 'bg-yellow-50',
          borderColor: 'border-yellow-200',
          textColor: 'text-yellow-800'
        };
      
      case 'REJECTED':
        return {
          icon: <XCircle className="h-8 w-8 text-red-500" />,
          title: '승인 거부',
          description: '구독 신청이 거부되었습니다',
          message: rejectionReason || '구독 신청이 거부되었습니다. 자세한 사유는 고객센터로 문의해주세요.',
          variant: 'destructive' as const,
          bgColor: 'bg-red-50',
          borderColor: 'border-red-200',
          textColor: 'text-red-800'
        };
      
      case 'SUSPENDED':
        return {
          icon: <AlertTriangle className="h-8 w-8 text-orange-500" />,
          title: '서비스 일시 중지',
          description: '구독 서비스가 일시 중지되었습니다',
          message: suspensionReason || '구독 서비스가 일시 중지되었습니다. 자세한 사유는 고객센터로 문의해주세요.',
          variant: 'warning' as const,
          bgColor: 'bg-orange-50',
          borderColor: 'border-orange-200',
          textColor: 'text-orange-800'
        };
      
      case 'TERMINATED':
        return {
          icon: <Ban className="h-8 w-8 text-gray-500" />,
          title: '구독 종료',
          description: '구독이 완전히 종료되었습니다',
          message: terminationReason || '구독이 종료되었습니다. 새로운 구독을 원하시면 다시 신청해주세요.',
          variant: 'secondary' as const,
          bgColor: 'bg-gray-50',
          borderColor: 'border-gray-200',
          textColor: 'text-gray-800'
        };
      
      default:
        return null;
    }
  };

  const config = getStatusConfig();
  
  // 정상 상태인 경우 컴포넌트를 렌더링하지 않음
  if (!config) {
    return null;
  }

  return (
    <Card className={`${config.bgColor} ${config.borderColor} border-2`}>
      <CardHeader>
        <div className="flex items-center space-x-3">
          {config.icon}
          <div>
            <CardTitle className={`text-xl ${config.textColor}`}>
              {config.title}
            </CardTitle>
            <CardDescription className={config.textColor}>
              {config.description}
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        <div className={`p-4 rounded-lg bg-white/50 ${config.textColor}`}>
          <p className="text-sm leading-relaxed">
            {config.message}
          </p>
        </div>

        {/* 상태별 액션 버튼 */}
        <div className="flex flex-col sm:flex-row gap-3">
          {status === 'REJECTED' && onReapply && (
            <Button 
              onClick={onReapply}
              className="flex items-center space-x-2"
            >
              <RefreshCw className="h-4 w-4" />
              <span>재신청하기</span>
            </Button>
          )}
          
          {status === 'TERMINATED' && onNewSubscription && (
            <Button 
              onClick={onNewSubscription}
              className="flex items-center space-x-2"
            >
              <Plus className="h-4 w-4" />
              <span>새 구독 신청</span>
            </Button>
          )}
          
          {(status === 'SUSPENDED' || status === 'REJECTED') && (
            <div className="flex flex-col sm:flex-row gap-2">
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => window.open(`mailto:${contactEmail}`)}
                className="flex items-center space-x-2"
              >
                <Mail className="h-4 w-4" />
                <span>이메일 문의</span>
              </Button>
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => window.open(`tel:${contactPhone}`)}
                className="flex items-center space-x-2"
              >
                <Phone className="h-4 w-4" />
                <span>전화 문의</span>
              </Button>
            </div>
          )}
        </div>

        {/* 연락처 정보 */}
        <div className={`text-xs ${config.textColor} opacity-75 space-y-1`}>
          <div className="flex items-center space-x-2">
            <Mail className="h-3 w-3" />
            <span>이메일: {contactEmail}</span>
          </div>
          <div className="flex items-center space-x-2">
            <Phone className="h-3 w-3" />
            <span>전화: {contactPhone}</span>
          </div>
          <div className="mt-2">
            <span>고객센터 운영시간: 평일 09:00 - 18:00 (주말, 공휴일 제외)</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default SubscriptionStatusDisplay;