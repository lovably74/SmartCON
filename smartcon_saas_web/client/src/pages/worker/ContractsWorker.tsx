import DashboardLayout from "@/components/layout/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { FileCheck, FileText, PenTool } from "lucide-react";

export default function ContractsWorker() {
  const contracts = [
    { id: 1, title: "2025년 표준근로계약서", site: "강남 테헤란로 오피스", date: "2025.12.18", status: "서명대기" },
    { id: 2, title: "개인정보 수집 이용 동의서", site: "강남 테헤란로 오피스", date: "2025.12.18", status: "서명대기" },
    { id: 3, title: "안전보건교육 이수 확인서", site: "강남 테헤란로 오피스", date: "2025.12.17", status: "서명완료" },
    { id: 4, title: "보안 서약서", site: "강남 테헤란로 오피스", date: "2025.12.16", status: "서명완료" },
    { id: 5, title: "2024년 표준근로계약서", site: "판교 데이터센터", date: "2024.01.05", status: "만료" },
  ];

  return (
    <DashboardLayout role="worker">
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">전자 계약</h2>
          <p className="text-muted-foreground">서명이 필요한 계약서 및 동의서를 확인합니다.</p>
        </div>

        <div className="grid gap-4">
          {contracts.map((contract) => (
            <Card key={contract.id} className={contract.status === "서명대기" ? "border-primary/50 bg-primary/5" : ""}>
              <CardContent className="flex items-center justify-between p-6">
                <div className="flex items-center gap-4">
                  <div className={`h-10 w-10 rounded-full flex items-center justify-center ${
                    contract.status === "서명대기" ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground"
                  }`}>
                    {contract.status === "서명대기" ? <PenTool className="h-5 w-5" /> : <FileText className="h-5 w-5" />}
                  </div>
                  <div>
                    <div className="font-bold text-lg">{contract.title}</div>
                    <div className="text-sm text-muted-foreground">{contract.site} | {contract.date}</div>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <Badge variant={
                    contract.status === "서명대기" ? "default" :
                    contract.status === "서명완료" ? "outline" : "secondary"
                  } className={
                    contract.status === "서명완료" ? "text-green-600 border-green-200 bg-green-50" : ""
                  }>
                    {contract.status}
                  </Badge>
                  <Button variant={contract.status === "서명대기" ? "default" : "outline"}>
                    {contract.status === "서명대기" ? "서명하기" : "보기"}
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </DashboardLayout>
  );
}
