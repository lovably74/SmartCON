import React from 'react';
import { Badge } from '@/components/ui/badge';
import { 
  CheckCircle, 
  Clock, 
  XCircle, 
  AlertTriangle, 
  Ban,
  Pause,
  Calendar
} from 'lucide-react';
import { SubscriptionStatus } from './SubscriptionStatusDisplay';

interface SubscriptionStatusIconProps {
  status: SubscriptionStatus;
  size?: 'sm' | 'md' | 'lg';
  showText?: boolean;
  className?: string;
}

/**
 * 구독 상태를 시각적으로 표시하는 아이콘 컴포넌트
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
const SubscriptionStatusIcon: React.FC<SubscriptionStatusIconProps> = ({
  status,
  size = 'md',
  showText = true,
  className = ''
}) => {
  const getStatusConfig = () => {
    const iconSizes = {
      sm: 'w-3 h-3',
      md: 'w-4 h-4',
      lg: 'w-5 h-5'
    };

    const iconSize = iconSizes[size];

    switch (status) {
      case 'ACTIVE':
        return {
          icon: <CheckCircle className={`${iconSize} mr-1`} />,
          text: '활성',
          className: 'bg-green-100 text-green-800 hover:bg-green-200'
        };
      
      case 'PENDING_APPROVAL':
        return {
          icon: <Clock className={`${iconSize} mr-1`} />,
          text: '승인 대기',
          className: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200'
        };
      
      case 'REJECTED':
        return {
          icon: <XCircle className={`${iconSize} mr-1`} />,
          text: '승인 거부',
          className: 'bg-red-100 text-red-800 hover:bg-red-200'
        };
      
      case 'SUSPENDED':
        return {
          icon: <AlertTriangle className={`${iconSize} mr-1`} />,
          text: '일시 중지',
          className: 'bg-orange-100 text-orange-800 hover:bg-orange-200'
        };
      
      case 'TERMINATED':
        return {
          icon: <Ban className={`${iconSize} mr-1`} />,
          text: '종료',
          className: 'bg-gray-100 text-gray-800 hover:bg-gray-200'
        };
      
      case 'CANCELLED':
        return {
          icon: <Pause className={`${iconSize} mr-1`} />,
          text: '해지',
          className: 'bg-gray-100 text-gray-800 hover:bg-gray-200'
        };
      
      case 'EXPIRED':
        return {
          icon: <Calendar className={`${iconSize} mr-1`} />,
          text: '만료',
          className: 'bg-gray-100 text-gray-800 hover:bg-gray-200'
        };
      
      default:
        return {
          icon: <Clock className={`${iconSize} mr-1`} />,
          text: status,
          className: 'bg-gray-100 text-gray-800 hover:bg-gray-200'
        };
    }
  };

  const config = getStatusConfig();

  return (
    <Badge className={`${config.className} ${className}`}>
      {config.icon}
      {showText && <span>{config.text}</span>}
    </Badge>
  );
};

export default SubscriptionStatusIcon;