import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Home, ArrowLeft } from "lucide-react";
import { Link } from "wouter";

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
      <Card className="w-full max-w-md text-center">
        <CardContent className="p-8">
          <div className="mb-6">
            <div className="text-6xl font-bold text-muted-foreground mb-2">404</div>
            <h1 className="text-2xl font-bold mb-2">페이지를 찾을 수 없습니다</h1>
            <p className="text-muted-foreground">
              요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.
            </p>
          </div>
          
          <div className="space-y-3">
            <Link href="/">
              <Button className="w-full">
                <Home className="h-4 w-4 mr-2" />
                홈으로 돌아가기
              </Button>
            </Link>
            <Button variant="outline" className="w-full" onClick={() => window.history.back()}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              이전 페이지로
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}