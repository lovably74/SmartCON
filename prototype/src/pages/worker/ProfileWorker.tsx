import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import {
  User,
  Camera,
  Shield,
  FileText,
  Phone,
  Mail,
  MapPin,
  Calendar,
  Award,
  AlertTriangle,
  CheckCircle2,
  Edit
} from "lucide-react";
import { useState } from "react";

export default function ProfileWorker() {
  const [isEditing, setIsEditing] = useState(false);

  // Mock Data
  const userProfile = {
    name: "박민수",
    email: "worker@smartcon.com",
    phone: "010-4567-8901",
    address: "서울시 강남구 테헤란로 456",
    birthDate: "1985-01-01",
    joinDate: "2024-03-15",
    employeeId: "W2024001",
    emergencyContact: "010-1234-5678",
    emergencyName: "박영희 (배우자)",
    isFaceRegistered: true,
    profileImage: null
  };

  const workHistory = [
    {
      site: "강남 테헤란로 오피스 신축공사",
      period: "2025-01-01 ~ 현재",
      role: "철근공",
      team: "철근팀",
      status: "진행중"
    },
    {
      site: "판교 데이터센터 건립공사",
      period: "2024-10-01 ~ 2024-12-31",
      role: "철근공",
      team: "철근팀",
      status: "완료"
    },
    {
      site: "부산 에코델타시티 조성공사",
      period: "2024-06-01 ~ 2024-09-30",
      role: "철근공",
      team: "철근팀",
      status: "완료"
    }
  ];

  const certifications = [
    { name: "건설기계조종사면허", issueDate: "2020-03-15", expiryDate: "2025-03-14", status: "유효" },
    { name: "안전보건교육이수증", issueDate: "2024-01-10", expiryDate: "2025-01-09", status: "유효" },
    { name: "용접기능사", issueDate: "2018-05-20", expiryDate: null, status: "유효" }
  ];

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        {/* 프로필 헤더 */}
        <Card>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-6">
              <div className="flex flex-col items-center">
                <Avatar className="h-24 w-24 mb-4">
                  <AvatarImage src={userProfile.profileImage || ""} />
                  <AvatarFallback className="text-2xl font-bold">
                    {userProfile.name.charAt(0)}
                  </AvatarFallback>
                </Avatar>
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      <Camera className="h-4 w-4 mr-2" />
                      사진 변경
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>프로필 사진 변경</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="text-center">
                        <Avatar className="h-32 w-32 mx-auto mb-4">
                          <AvatarFallback className="text-4xl font-bold">
                            {userProfile.name.charAt(0)}
                          </AvatarFallback>
                        </Avatar>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <Button variant="outline">
                          <Camera className="h-4 w-4 mr-2" />
                          카메라로 촬영
                        </Button>
                        <Button variant="outline">
                          <FileText className="h-4 w-4 mr-2" />
                          파일에서 선택
                        </Button>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>

              <div className="flex-1 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h1 className="text-2xl font-bold">{userProfile.name}</h1>
                    <p className="text-muted-foreground">사원번호: {userProfile.employeeId}</p>
                  </div>
                  <Button 
                    variant="outline" 
                    onClick={() => setIsEditing(!isEditing)}
                  >
                    <Edit className="h-4 w-4 mr-2" />
                    {isEditing ? "저장" : "편집"}
                  </Button>
                </div>

                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Mail className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">{userProfile.email}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Phone className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">{userProfile.phone}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">{userProfile.address}</span>
                    </div>
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">입사일: {userProfile.joinDate}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Shield className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">안면인식 등록: </span>
                      <Badge variant={userProfile.isFaceRegistered ? "default" : "destructive"}>
                        {userProfile.isFaceRegistered ? "완료" : "미등록"}
                      </Badge>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid gap-6 lg:grid-cols-2">
          {/* 개인정보 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                개인정보
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {isEditing ? (
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">성명</Label>
                    <Input id="name" defaultValue={userProfile.name} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="phone">연락처</Label>
                    <Input id="phone" defaultValue={userProfile.phone} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">이메일</Label>
                    <Input id="email" type="email" defaultValue={userProfile.email} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="address">주소</Label>
                    <Input id="address" defaultValue={userProfile.address} />
                  </div>
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">성명</span>
                    <span className="font-medium">{userProfile.name}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">생년월일</span>
                    <span className="font-medium">{userProfile.birthDate}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">연락처</span>
                    <span className="font-medium">{userProfile.phone}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">이메일</span>
                    <span className="font-medium">{userProfile.email}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">주소</span>
                    <span className="font-medium text-right">{userProfile.address}</span>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* 비상연락처 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Phone className="h-5 w-5" />
                비상연락처
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {isEditing ? (
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="emergency-name">비상연락처 성명</Label>
                    <Input id="emergency-name" defaultValue={userProfile.emergencyName} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="emergency-phone">비상연락처 번호</Label>
                    <Input id="emergency-phone" defaultValue={userProfile.emergencyContact} />
                  </div>
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">성명</span>
                    <span className="font-medium">{userProfile.emergencyName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">연락처</span>
                    <span className="font-medium">{userProfile.emergencyContact}</span>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 근무 이력 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              근무 이력
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {workHistory.map((work, i) => (
                <div key={i} className="flex items-center justify-between p-4 border rounded-lg">
                  <div>
                    <div className="font-semibold">{work.site}</div>
                    <div className="text-sm text-muted-foreground mt-1">
                      {work.role} • {work.team} • {work.period}
                    </div>
                  </div>
                  <Badge variant={work.status === "진행중" ? "default" : "secondary"}>
                    {work.status}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* 자격증 및 교육 이수 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Award className="h-5 w-5" />
              자격증 및 교육 이수
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {certifications.map((cert, i) => (
                <div key={i} className="flex items-center justify-between p-4 border rounded-lg">
                  <div>
                    <div className="font-semibold">{cert.name}</div>
                    <div className="text-sm text-muted-foreground mt-1">
                      발급일: {cert.issueDate}
                      {cert.expiryDate && ` • 만료일: ${cert.expiryDate}`}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {cert.status === "유효" ? (
                      <CheckCircle2 className="h-4 w-4 text-green-600" />
                    ) : (
                      <AlertTriangle className="h-4 w-4 text-orange-600" />
                    )}
                    <Badge variant={cert.status === "유효" ? "default" : "destructive"}>
                      {cert.status}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* 안면인식 등록 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              안면인식 등록
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
              <div>
                <div className="font-semibold mb-1">
                  안면인식 등록 상태: 
                  <Badge className="ml-2" variant={userProfile.isFaceRegistered ? "default" : "destructive"}>
                    {userProfile.isFaceRegistered ? "등록 완료" : "미등록"}
                  </Badge>
                </div>
                <p className="text-sm text-muted-foreground">
                  {userProfile.isFaceRegistered 
                    ? "안면인식이 등록되어 출퇴근 체크가 가능합니다."
                    : "안면인식을 등록하면 편리하게 출퇴근 체크를 할 수 있습니다."
                  }
                </p>
              </div>
              <Button variant={userProfile.isFaceRegistered ? "outline" : "default"}>
                <Camera className="h-4 w-4 mr-2" />
                {userProfile.isFaceRegistered ? "재등록" : "등록하기"}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}