import DashboardLayout from "@/components/layout/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import {
  CalendarDays,
  Clock,
  MapPin,
  UserCircle2,
  Wallet,
  CheckCircle2,
  AlertCircle,
  PenTool
} from "lucide-react";

export default function DashboardWorker() {
  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        {/* Profile Summary */}
        <div className="bg-white p-6 rounded-xl border shadow-sm flex flex-col md:flex-row items-center gap-6">
          <div className="relative">
            <div className="h-24 w-24 rounded-full bg-muted flex items-center justify-center overflow-hidden border-4 border-white shadow-md">
              <UserCircle2 className="h-20 w-20 text-muted-foreground" />
            </div>
            <div className="absolute bottom-0 right-0 h-8 w-8 bg-green-500 rounded-full border-4 border-white flex items-center justify-center text-white">
              <CheckCircle2 className="h-4 w-4" />
            </div>
          </div>
          <div className="flex-1 text-center md:text-left space-y-1">
            <h2 className="text-2xl font-bold">홍길동 (형틀목공)</h2>
            <p className="text-muted-foreground flex items-center justify-center md:justify-start gap-2">
              <MapPin className="h-4 w-4" /> 강남 테헤란로 오피스 신축공사
            </p>
            <div className="flex items-center justify-center md:justify-start gap-2 text-sm">
              <span className="bg-blue-100 text-blue-700 px-2 py-0.5 rounded">안면인식 등록완료</span>
              <span className="bg-green-100 text-green-700 px-2 py-0.5 rounded">기초안전교육 이수</span>
            </div>
          </div>
          <div className="flex flex-col gap-2 w-full md:w-auto">
            <Button size="lg" className="w-full md:w-auto bg-green-600 hover:bg-green-700">
              <Clock className="mr-2 h-5 w-5" /> 출근 체크
            </Button>
            <Button variant="outline" className="w-full md:w-auto">
              <PenTool className="mr-2 h-4 w-4" /> 전자계약 서명하기
            </Button>
          </div>
        </div>

        {/* Monthly Stats */}
        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">이번 달 누적 공수</CardTitle>
              <CalendarDays className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">18.5 <span className="text-lg font-normal text-muted-foreground">공수</span></div>
              <p className="text-xs text-muted-foreground mt-1">전월 대비 +2.5 공수</p>
              <div className="mt-4 flex items-center gap-2 text-sm">
                <div className="flex-1 bg-muted rounded-full h-2 overflow-hidden">
                  <div className="bg-primary h-full" style={{ width: "75%" }}></div>
                </div>
                <span className="text-muted-foreground">목표 25공수</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">예상 수령액 (세전)</CardTitle>
              <Wallet className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-bold">4,625,000 <span className="text-lg font-normal text-muted-foreground">원</span></div>
              <p className="text-xs text-muted-foreground mt-1">단가: 250,000원</p>
              <div className="mt-4 flex items-center gap-2 text-xs text-muted-foreground bg-muted/50 p-2 rounded">
                <AlertCircle className="h-3 w-3" />
                <span>실제 지급액은 세금 및 4대보험 공제 후 입금됩니다.</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Recent Attendance */}
        <Card>
          <CardHeader>
            <CardTitle>최근 출역 이력</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[
                { date: "2025.12.18 (목)", time: "06:45 ~ 17:00", gongsu: 1.0, site: "강남 테헤란로 오피스", status: "정상" },
                { date: "2025.12.17 (수)", time: "06:50 ~ 17:00", gongsu: 1.0, site: "강남 테헤란로 오피스", status: "정상" },
                { date: "2025.12.16 (화)", time: "06:40 ~ 19:00", gongsu: 1.5, site: "강남 테헤란로 오피스", status: "연장" },
                { date: "2025.12.15 (월)", time: "06:55 ~ 17:00", gongsu: 1.0, site: "강남 테헤란로 오피스", status: "정상" },
                { date: "2025.12.13 (토)", time: "07:00 ~ 12:00", gongsu: 0.5, site: "강남 테헤란로 오피스", status: "오전" },
              ].map((item, i) => (
                <div key={i} className="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50 transition-colors">
                  <div className="space-y-1">
                    <div className="font-medium flex items-center gap-2">
                      {item.date}
                      <span className={`text-[10px] px-1.5 py-0.5 rounded border ${
                        item.status === "연장" ? "bg-purple-50 text-purple-600 border-purple-200" : 
                        item.status === "오전" ? "bg-orange-50 text-orange-600 border-orange-200" : 
                        "bg-green-50 text-green-600 border-green-200"
                      }`}>{item.status}</span>
                    </div>
                    <div className="text-sm text-muted-foreground flex items-center gap-1">
                      <Clock className="h-3 w-3" /> {item.time}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-lg">{item.gongsu} <span className="text-xs font-normal text-muted-foreground">공수</span></div>
                    <div className="text-xs text-muted-foreground">{item.site}</div>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="ghost" className="w-full mt-4 text-muted-foreground">더 보기</Button>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}

